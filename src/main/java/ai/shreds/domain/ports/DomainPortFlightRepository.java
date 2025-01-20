package ai.shreds.domain.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ai.shreds.domain.entities.DomainEntityFlight;
import ai.shreds.domain.value_objects.DomainValueFlightSearchCriteria;

/**
 * Port defining flight repository operations in the domain layer.
 * Implementations handle persistence of flight entities.
 */
public interface DomainPortFlightRepository {

    /**
     * Finds flights matching the specified search criteria.
     *
     * @param criteria the search criteria
     * @return list of matching flights
     * @throws ai.shreds.domain.exceptions.DomainFlightNotFoundException if no flights found
     */
    List<DomainEntityFlight> findFlightsByCriteria(DomainValueFlightSearchCriteria criteria);

    /**
     * Saves or updates a flight entity.
     *
     * @param flight the flight to save
     * @return the saved flight entity
     * @throws IllegalArgumentException if flight is invalid
     */
    DomainEntityFlight saveFlight(DomainEntityFlight flight);

    /**
     * Finds a flight by its ID.
     *
     * @param flightId the flight ID
     * @return optional containing the flight if found
     */
    default Optional<DomainEntityFlight> findById(UUID flightId) {
        return findFlightsByCriteria(DomainValueFlightSearchCriteria.builder().build())
                .stream()
                .filter(flight -> flight.getFlightId().equals(flightId))
                .findFirst();
    }

    /**
     * Deletes a flight by its ID.
     *
     * @param flightId the flight ID
     * @return true if flight was deleted, false if not found
     */
    default boolean deleteById(UUID flightId) {
        throw new UnsupportedOperationException("Delete operation not supported");
    }

    /**
     * Checks if a flight exists.
     *
     * @param flightId the flight ID
     * @return true if flight exists
     */
    default boolean existsById(UUID flightId) {
        return findById(flightId).isPresent();
    }
}
