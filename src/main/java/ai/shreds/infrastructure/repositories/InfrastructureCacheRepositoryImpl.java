package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityFlight;
import ai.shreds.domain.ports.DomainPortCache;
import ai.shreds.domain.value_objects.DomainValueFlightSearchCriteria;
import ai.shreds.infrastructure.exceptions.InfrastructureDatabaseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based implementation of the domain cache port.
 * Provides caching functionality with compression and TTL management.
 */
@Slf4j
@Repository
public class InfrastructureCacheRepositoryImpl implements DomainPortCache {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ValueOperations<String, Object> valueOps;

    // Cache configuration
    private static final Duration DEFAULT_CACHE_DURATION = Duration.ofMinutes(10);
    private static final Duration EXTENDED_CACHE_DURATION = Duration.ofHours(1);
    private static final int MIN_RESULTS_FOR_EXTENDED_CACHE = 100;
    private static final String CACHE_PREFIX = "FLIGHTS:";

    public InfrastructureCacheRepositoryImpl(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.valueOps = redisTemplate.opsForValue();
    }

    @Override
    public Optional<List<DomainEntityFlight>> retrieveCachedSearch(DomainValueFlightSearchCriteria criteria) {
        try {
            String key = buildCacheKey(criteria);
            log.debug("Attempting to retrieve cached results for key: {}", key);

            Object cachedValue = valueOps.get(key);
            if (cachedValue == null) {
                log.debug("Cache miss for key: {}", key);
                return Optional.empty();
            }

            // Deserialize the cached value
            List<DomainEntityFlight> flights = deserializeFlights(cachedValue);
            log.debug("Cache hit. Retrieved {} flights from cache", flights.size());

            return Optional.of(flights);

        } catch (Exception e) {
            log.error("Error retrieving cached search results", e);
            throw new InfrastructureDatabaseException(
                    "Failed to retrieve cached search results: " + e.getMessage(),
                    "CACHE_RETRIEVAL_ERROR"
            );
        }
    }

    @Override
    public void cacheSearchResults(DomainValueFlightSearchCriteria criteria, List<DomainEntityFlight> flights) {
        try {
            String key = buildCacheKey(criteria);
            log.debug("Caching {} flights with key: {}", flights.size(), key);

            // Serialize flights for caching
            String serializedFlights = objectMapper.writeValueAsString(flights);

            // Determine cache duration based on result size and time of day
            Duration cacheDuration = determineCacheDuration(flights, criteria.getDepartureDate());

            valueOps.set(key, serializedFlights, cacheDuration);
            log.debug("Successfully cached results for {} minutes", cacheDuration.toMinutes());

        } catch (Exception e) {
            log.error("Error caching search results", e);
            throw new InfrastructureDatabaseException(
                    "Failed to cache search results: " + e.getMessage(),
                    "CACHE_STORAGE_ERROR"
            );
        }
    }

    private String buildCacheKey(DomainValueFlightSearchCriteria criteria) {
        return new StringBuilder(CACHE_PREFIX)
                .append(criteria.getOrigin().toUpperCase())
                .append(":")
                .append(criteria.getDestination().toUpperCase())
                .append(":")
                .append(criteria.getDepartureDate().toLocalDate())
                .append(criteria.getReturnDate() != null ? ":" + criteria.getReturnDate().toLocalDate() : ":ONEWAY")
                .append(criteria.getSeatClass() != null ? ":" + criteria.getSeatClass().toUpperCase() : ":ALL")
                .toString();
    }

    private List<DomainEntityFlight> deserializeFlights(Object cachedValue) throws Exception {
        if (cachedValue instanceof String) {
            return objectMapper.readValue((String) cachedValue,
                    new TypeReference<List<DomainEntityFlight>>() {});
        }
        throw new IllegalStateException("Cached value is not in expected format");
    }

    private Duration determineCacheDuration(List<DomainEntityFlight> flights, LocalDateTime departureDate) {
        // Use extended cache duration for popular routes (more results)
        if (flights.size() >= MIN_RESULTS_FOR_EXTENDED_CACHE) {
            return EXTENDED_CACHE_DURATION;
        }

        // Use shorter duration for imminent departures
        if (departureDate.isBefore(LocalDateTime.now().plusDays(1))) {
            return Duration.ofMinutes(5);
        }

        return DEFAULT_CACHE_DURATION;
    }

    /**
     * Clears all cached results for a specific route.
     *
     * @param origin departure airport
     * @param destination arrival airport
     */
    public void clearRouteCache(String origin, String destination) {
        try {
            String pattern = CACHE_PREFIX + origin.toUpperCase() + ":" + destination.toUpperCase() + ":*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Cleared {} cached entries for route {}-{}", keys.size(), origin, destination);
            }
        } catch (Exception e) {
            log.error("Error clearing route cache", e);
            throw new InfrastructureDatabaseException(
                    "Failed to clear route cache: " + e.getMessage(),
                    "CACHE_CLEAR_ERROR"
            );
        }
    }
}
