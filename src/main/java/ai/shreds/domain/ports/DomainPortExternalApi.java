package ai.shreds.domain.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ai.shreds.domain.entities.DomainEntityFlight;

/**
 * Port defining external API operations in the domain layer.
 * Implementations handle integration with external flight data providers.
 */
public interface DomainPortExternalApi {

    /**
     * Enriches flight data with information from external API.
     *
     * @param flights list of flights to enrich
     * @return list of enriched flights
     */
    List<DomainEntityFlight> enrichFlightData(List<DomainEntityFlight> flights);

    /**
     * Enriches a single flight with external data.
     *
     * @param flight the flight to enrich
     * @return enriched flight
     */
    default DomainEntityFlight enrichSingleFlight(DomainEntityFlight flight) {
        return enrichFlightData(List.of(flight)).get(0);
    }

    /**
     * Retrieves real-time flight status.
     *
     * @param flightId the flight ID
     * @return optional containing flight status if available
     */
    default Optional<String> getFlightStatus(UUID flightId) {
        throw new UnsupportedOperationException("Flight status retrieval not implemented");
    }

    /**
     * Checks if the external API is available.
     *
     * @return true if API is available
     */
    default boolean isApiAvailable() {
        try {
            enrichFlightData(List.of());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the API provider name.
     *
     * @return the API provider name
     */
    default String getProviderName() {
        return "Unknown Provider";
    }
}
