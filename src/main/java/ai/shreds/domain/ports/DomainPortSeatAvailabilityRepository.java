package ai.shreds.domain.ports;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ai.shreds.domain.entities.DomainEntitySeatAvailability;

/**
 * Port defining seat availability repository operations in the domain layer.
 * Implementations handle persistence of seat availability entities.
 */
public interface DomainPortSeatAvailabilityRepository {

    /**
     * Finds all seat availability records for a flight.
     *
     * @param flightId the flight ID
     * @return list of seat availability records
     */
    List<DomainEntitySeatAvailability> findByFlightId(UUID flightId);

    /**
     * Saves or updates a seat availability record.
     *
     * @param seatAvailability the seat availability to save
     * @return the saved seat availability record
     * @throws IllegalArgumentException if seat availability is invalid
     */
    DomainEntitySeatAvailability saveAvailability(DomainEntitySeatAvailability seatAvailability);

    /**
     * Finds seat availability for a specific flight and seat class.
     *
     * @param flightId the flight ID
     * @param seatClass the seat class
     * @return optional containing the seat availability if found
     */
    default Optional<DomainEntitySeatAvailability> findByFlightIdAndSeatClass(UUID flightId, String seatClass) {
        return findByFlightId(flightId).stream()
                .filter(availability -> availability.getSeatClass().equals(seatClass))
                .findFirst();
    }

    /**
     * Updates seat count for a specific flight and seat class.
     *
     * @param flightId the flight ID
     * @param seatClass the seat class
     * @param newCount the new seat count
     * @return updated seat availability record
     * @throws IllegalArgumentException if parameters are invalid
     */
    default DomainEntitySeatAvailability updateSeatCount(UUID flightId, String seatClass, int newCount) {
        DomainEntitySeatAvailability availability = findByFlightIdAndSeatClass(flightId, seatClass)
                .orElseThrow(() -> new IllegalArgumentException("Seat availability not found"));
        availability.updateAvailableSeats(newCount);
        return saveAvailability(availability);
    }

    /**
     * Checks if seats are available for a specific flight and class.
     *
     * @param flightId the flight ID
     * @param seatClass the seat class
     * @param requiredSeats number of seats needed
     * @return true if enough seats are available
     */
    default boolean hasAvailableSeats(UUID flightId, String seatClass, int requiredSeats) {
        return findByFlightIdAndSeatClass(flightId, seatClass)
                .map(availability -> availability.hasAvailability(requiredSeats))
                .orElse(false);
    }
}
