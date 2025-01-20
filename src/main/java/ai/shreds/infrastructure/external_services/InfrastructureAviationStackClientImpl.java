package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.entities.DomainEntityFlight;
import ai.shreds.domain.ports.DomainPortExternalApi;
import ai.shreds.infrastructure.exceptions.InfrastructureDatabaseException;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementation of external API port using AviationStack API.
 * Includes rate limiting, retries, and parallel processing.
 */
@Slf4j
@Service
public class InfrastructureAviationStackClientImpl implements DomainPortExternalApi {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;
    private final RateLimiter rateLimiter;
    private final Retry retry;

    public InfrastructureAviationStackClientImpl(
            RestTemplate restTemplate,
            @Value("${aviationstack.api.key}") String apiKey,
            @Value("${aviationstack.api.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.rateLimiter = createRateLimiter();
        this.retry = createRetry();
    }

    @Override
    public List<DomainEntityFlight> enrichFlightData(List<DomainEntityFlight> flights) {
        try {
            log.debug("Enriching {} flights with AviationStack data", flights.size());

            // Process flights in parallel with rate limiting
            List<CompletableFuture<DomainEntityFlight>> futures = flights.stream()
                    .map(this::enrichFlightAsync)
                    .collect(Collectors.toList());

            // Wait for all enrichment operations to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // Collect results
            List<DomainEntityFlight> enrichedFlights = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            log.debug("Successfully enriched {} flights", enrichedFlights.size());
            return enrichedFlights;

        } catch (Exception e) {
            log.error("Error enriching flights with AviationStack API", e);
            throw new InfrastructureDatabaseException(
                    "Failed to enrich flight data: " + e.getMessage(),
                    "EXTERNAL_API_ERROR"
            );
        }
    }

    private CompletableFuture<DomainEntityFlight> enrichFlightAsync(DomainEntityFlight flight) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Retry.decorateSupplier(retry, () ->
                        RateLimiter.decorateSupplier(rateLimiter, () ->
                                enrichSingleFlight(flight)).get()).get();
            } catch (Exception e) {
                log.error("Error enriching flight {}", flight.getFlightId(), e);
                return flight; // Return original flight if enrichment fails
            }
        });
    }

    private DomainEntityFlight enrichSingleFlight(DomainEntityFlight flight) {
        String url = buildApiUrl(flight);
        ResponseEntity<AviationStackResponse> response = restTemplate.getForEntity(url, AviationStackResponse.class);

        if (response.getBody() != null && response.getBody().getData() != null && !response.getBody().getData().isEmpty()) {
            FlightData apiData = response.getBody().getData().get(0);
            updateFlightWithApiData(flight, apiData);
        }

        return flight;
    }

    private String buildApiUrl(DomainEntityFlight flight) {
        return String.format("%s?access_key=%s&flight_iata=%s&dep_iata=%s&arr_iata=%s",
                baseUrl,
                apiKey,
                flight.getFlightId(),
                flight.getOrigin(),
                flight.getDestination());
    }

    private void updateFlightWithApiData(DomainEntityFlight flight, FlightData apiData) {
        // Update flight with real-time data from API
        if (apiData.getDeparture() != null) {
            flight.setDepartureTime(apiData.getDeparture().getScheduled());
        }
        if (apiData.getArrival() != null) {
            flight.setArrivalTime(apiData.getArrival().getScheduled());
        }
        // Add more enrichment as needed
    }

    private RateLimiter createRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(100) // API rate limit
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ofSeconds(5))
                .build();
        return RateLimiter.of("aviationstack", config);
    }

    private Retry createRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(2))
                .build();
        return Retry.of("aviationstack", config);
    }

    @Data
    private static class AviationStackResponse {
        private List<FlightData> data;
    }

    @Data
    private static class FlightData {
        private FlightTiming departure;
        private FlightTiming arrival;
        private String flight_status;
        @JsonProperty("airline")
        private Map<String, Object> airline;
    }

    @Data
    private static class FlightTiming {
        private LocalDateTime scheduled;
        private LocalDateTime estimated;
        private String terminal;
        private String gate;
    }
}
