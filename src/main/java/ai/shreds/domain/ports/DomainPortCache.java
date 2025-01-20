package ai.shreds.domain.ports;

import java.util.List;
import java.util.Optional;
import java.time.Duration;

import ai.shreds.domain.entities.DomainEntityFlight;
import ai.shreds.domain.value_objects.DomainValueFlightSearchCriteria;

/**
 * Port defining caching operations in the domain layer.
 * Implementations handle caching of flight search results.
 */
public interface DomainPortCache {

    /**
     * Retrieves cached search results for given criteria.
     *
     * @param criteria the search criteria
     * @return optional containing cached flights if found
     */
    Optional<List<DomainEntityFlight>> retrieveCachedSearch(DomainValueFlightSearchCriteria criteria);

    /**
     * Caches search results for given criteria.
     *
     * @param criteria the search criteria
     * @param flights the flights to cache
     */
    void cacheSearchResults(DomainValueFlightSearchCriteria criteria, List<DomainEntityFlight> flights);

    /**
     * Caches search results with a specific TTL.
     *
     * @param criteria the search criteria
     * @param flights the flights to cache
     * @param ttl the time-to-live for the cache entry
     */
    default void cacheSearchResults(DomainValueFlightSearchCriteria criteria,
                                   List<DomainEntityFlight> flights,
                                   Duration ttl) {
        cacheSearchResults(criteria, flights);
    }

    /**
     * Invalidates cached results for given criteria.
     *
     * @param criteria the search criteria
     */
    default void invalidateCache(DomainValueFlightSearchCriteria criteria) {
        cacheSearchResults(criteria, List.of());
    }

    /**
     * Checks if cache contains results for given criteria.
     *
     * @param criteria the search criteria
     * @return true if cache contains results
     */
    default boolean hasCachedResults(DomainValueFlightSearchCriteria criteria) {
        return retrieveCachedSearch(criteria).isPresent();
    }

    /**
     * Clears all cached search results.
     */
    default void clearAllCache() {
        // Default implementation does nothing
        // Implementations should override this method if they support cache clearing
    }
}
