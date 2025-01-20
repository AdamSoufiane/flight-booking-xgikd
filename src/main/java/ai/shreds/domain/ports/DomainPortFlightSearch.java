package ai.shreds.domain.ports;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import ai.shreds.domain.entities.DomainEntityFlight;
import ai.shreds.domain.value_objects.DomainValueFlightSearchCriteria;
import ai.shreds.domain.exceptions.DomainFlightNotFoundException;
import ai.shreds.domain.exceptions.DomainSearchValidationException;

/**
 * Port defining flight search operations in the domain layer.
 * Implementations handle searching and filtering of flights based on criteria.
 */
public interface DomainPortFlightSearch {

    /**
     * Searches for flights matching the specified criteria.
     *
     * @param criteria the search criteria
     * @return list of matching flights
     * @throws DomainFlightNotFoundException if no flights are found
     * @throws DomainSearchValidationException if search criteria are invalid
     */
    List<DomainEntityFlight> searchFlights(DomainValueFlightSearchCriteria criteria);

    /**
     * Searches for flights with available seats in specified class.
     *
     * @param criteria the search criteria
     * @param seatClass the required seat class
     * @param requiredSeats number of seats needed
     * @return list of flights with available seats
     */
    default List<DomainEntityFlight> searchFlightsWithAvailability(
            DomainValueFlightSearchCriteria criteria,
            String seatClass,
            int requiredSeats) {
        return searchFlights(criteria).stream()
                .filter(flight -> flight.hasAvailabilityForClass(seatClass))
                .filter(flight -> flight.getAvailableSeatsForClass(seatClass) >= requiredSeats)
                .toList();
    }

    /**
     * Searches for connecting flights between origin and destination.
     *
     * @param criteria the search criteria
     * @param maxConnections maximum number of connections
     * @return list of possible flight combinations
     */
    default List<List<DomainEntityFlight>> searchConnectingFlights(
            DomainValueFlightSearchCriteria criteria,
            int maxConnections) {
        throw new UnsupportedOperationException("Connecting flights search not implemented");
    }

    /**
     * Finds the earliest available flight for given criteria.
     *
     * @param criteria the search criteria
     * @return optional containing the earliest flight if found
     */
    default Optional<DomainEntityFlight> findEarliestFlight(DomainValueFlightSearchCriteria criteria) {
        return searchFlights(criteria).stream()
                .min((f1, f2) -> f1.getDepartureTime().compareTo(f2.getDepartureTime()));
    }

    /**
     * Checks if any flights are available for the given criteria.
     *
     * @param criteria the search criteria
     * @return true if flights are available
     */
    default boolean hasAvailableFlights(DomainValueFlightSearchCriteria criteria) {
        try {
            return !searchFlights(criteria).isEmpty();
        } catch (DomainFlightNotFoundException e) {
            return false;
        }
    }
}
