package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntitySeatAvailability;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Port interface for seat availability repository operations in the domain layer.
 */
public interface DomainPortSeatAvailabilityRepository {

    /**
     * Creates a new seat availability record.
     *
     * @param entity Seat availability to create
     * @throws ai.shreds.domain.exceptions.DomainException if creation fails
     */
    void createSeatAvailability(DomainEntitySeatAvailability entity);

    /**
     * Updates an existing seat availability record.
     *
     * @param entity Seat availability to update
     * @throws ai.shreds.domain.exceptions.DomainException if update fails
     */
    void updateSeatAvailability(DomainEntitySeatAvailability entity);

    /**
     * Finds seat availability records by flight ID string.
     *
     * @param flightId Flight ID
     * @return List of seat availability records
     */
    List<DomainEntitySeatAvailability> findSeatAvailabilityByFlightId(String flightId);

    /**
     * Saves a seat availability record (creates or updates).
     *
     * @param seatAvailability Seat availability to save
     * @return Saved seat availability record
     */
    DomainEntitySeatAvailability save(DomainEntitySeatAvailability seatAvailability);

    /**
     * Finds seat availability records by flight UUID.
     *
     * @param flightId Flight UUID
     * @return List of seat availability records
     */
    List<DomainEntitySeatAvailability> findByFlightId(UUID flightId);

    /**
     * Finds seat availability by flight ID and seat class.
     *
     * @param flightId Flight ID
     * @param seatClass Class of seats
     * @return Seat availability record
     */
    DomainEntitySeatAvailability findByFlightIdAndSeatClass(UUID flightId, String seatClass);

    /**
     * Finds all seat availability records with less than specified available seats.
     *
     * @param threshold Maximum number of available seats
     * @return List of seat availability records
     */
    List<DomainEntitySeatAvailability> findLowAvailability(int threshold);

    /**
     * Updates seat count for a specific flight and class.
     *
     * @param flightId Flight ID
     * @param seatClass Class of seats
     * @param count New seat count
     */
    void updateSeatCount(UUID flightId, String seatClass, int count);

    /**
     * Finds seat availability records updated after a specific time.
     *
     * @param timestamp Time threshold
     * @return List of recently updated seat availability records
     */
    List<DomainEntitySeatAvailability> findByLastUpdatedAfter(LocalDateTime timestamp);

    /**
     * Finds seat availability records by multiple flight IDs.
     *
     * @param flightIds List of flight IDs
     * @return List of seat availability records
     */
    List<DomainEntitySeatAvailability> findByFlightIds(List<UUID> flightIds);

    /**
     * Counts available seats for a flight.
     *
     * @param flightId Flight ID
     * @param seatClass Optional seat class filter
     * @return Number of available seats
     */
    int countAvailableSeats(UUID flightId, String seatClass);

    /**
     * Checks if a flight has any available seats.
     *
     * @param flightId Flight ID
     * @return true if seats are available
     */
    boolean hasAvailableSeats(UUID flightId);

    /**
     * Deletes seat availability records for a flight.
     *
     * @param flightId Flight ID
     */
    void deleteByFlightId(UUID flightId);

    /**
     * Finds flights with specific seat availability criteria.
     *
     * @param seatClass Class of seats
     * @param minAvailable Minimum number of seats that must be available
     * @return List of flight IDs meeting the criteria
     */
    List<UUID> findFlightsWithAvailability(String seatClass, int minAvailable);
}
