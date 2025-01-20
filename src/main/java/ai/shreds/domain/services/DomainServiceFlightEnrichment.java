package ai.shreds.domain.services;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ai.shreds.domain.entities.DomainEntityFlight;
import ai.shreds.domain.exceptions.DomainFlightNotFoundException;
import ai.shreds.domain.ports.DomainPortExternalApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * Service responsible for enriching flight data with external API information.
 * Implements retry logic and error handling for external API calls.
 */
@Slf4j
@Service
public class DomainServiceFlightEnrichment {

    private final DomainPortExternalApi externalApi;

    public DomainServiceFlightEnrichment(DomainPortExternalApi externalApi) {
        this.externalApi = Objects.requireNonNull(externalApi, "External API port cannot be null");
    }

    /**
     * Enriches flight data with information from external API.
     * Implements retry logic for resilience.
     *
     * @param flights list of flights to enrich
     * @return enriched flight list
     * @throws DomainFlightNotFoundException if enrichment fails
     */
    @Retryable(value = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public List<DomainEntityFlight> enrichFlightsWithExternalApi(List<DomainEntityFlight> flights) {
        if (flights == null || flights.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            log.debug("Enriching {} flights with external API data", flights.size());
            
            // Validate all flights before enrichment
            flights.forEach(DomainEntityFlight::validate);

            // Enrich flights in batches to avoid overwhelming the external API
            List<DomainEntityFlight> enrichedFlights = processBatches(flights);

            log.debug("Successfully enriched {} flights", enrichedFlights.size());
            return enrichedFlights;

        } catch (Exception e) {
            log.error("Failed to enrich flights with external API", e);
            throw new DomainFlightNotFoundException("Failed to enrich flight data: " + e.getMessage());
        }
    }

    /**
     * Processes flights in batches to avoid overwhelming the external API.
     *
     * @param flights list of flights to process
     * @return enriched flight list
     */
    private List<DomainEntityFlight> processBatches(List<DomainEntityFlight> flights) {
        final int BATCH_SIZE = 50;
        return flights.stream()
                .collect(Collectors.groupingBy(flight ->
                        flights.indexOf(flight) / BATCH_SIZE))
                .values()
                .stream()
                .map(this::enrichBatch)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * Enriches a batch of flights with external API data.
     *
     * @param batch list of flights to enrich
     * @return enriched flight list
     */
    private List<DomainEntityFlight> enrichBatch(List<DomainEntityFlight> batch) {
        try {
            Thread.sleep(100); // Rate limiting
            return externalApi.enrichFlightData(batch);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DomainFlightNotFoundException("Flight enrichment interrupted");
        }
    }
}
