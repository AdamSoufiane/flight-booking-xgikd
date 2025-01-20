package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntitySeatAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for seat availability entities.
 * Provides methods for querying and updating seat availability information.
 */
@Repository
public interface InfrastructureSeatAvailabilityJpaRepository extends JpaRepository<DomainEntitySeatAvailability, UUID> {

    /**
     * Finds all seat availability records for a flight.
     *
     * @param flightId the flight ID
     * @return list of seat availability records
     */
    List<DomainEntitySeatAvailability> findByFlightId(UUID flightId);

    /**
     * Finds seat availability for a specific flight and seat class with pessimistic lock.
     *
     * @param flightId the flight ID
     * @param seatClass the seat class
     * @return optional containing the seat availability if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sa FROM DomainEntitySeatAvailability sa " +
           "WHERE sa.flightId = :flightId AND sa.seatClass = :seatClass")
    Optional<DomainEntitySeatAvailability> findByFlightIdAndSeatClassForUpdate(
            @Param("flightId") UUID flightId,
            @Param("seatClass") String seatClass);

    /**
     * Finds all seat availability records for multiple flights.
     *
     * @param flightIds list of flight IDs
     * @return list of seat availability records
     */
    @Query("SELECT sa FROM DomainEntitySeatAvailability sa WHERE sa.flightId IN :flightIds")
    List<DomainEntitySeatAvailability> findByFlightIds(@Param("flightIds") List<UUID> flightIds);

    /**
     * Finds seat availability records with available seats for a flight.
     *
     * @param flightId the flight ID
     * @return list of seat availability records with available seats
     */
    @Query("SELECT sa FROM DomainEntitySeatAvailability sa " +
           "WHERE sa.flightId = :flightId AND sa.availableSeats > 0")
    List<DomainEntitySeatAvailability> findAvailableSeats(@Param("flightId") UUID flightId);

    /**
     * Counts available seats for a specific flight and seat class.
     *
     * @param flightId the flight ID
     * @param seatClass the seat class
     * @return number of available seats
     */
    @Query("SELECT sa.availableSeats FROM DomainEntitySeatAvailability sa " +
           "WHERE sa.flightId = :flightId AND sa.seatClass = :seatClass")
    Optional<Integer> countAvailableSeats(
            @Param("flightId") UUID flightId,
            @Param("seatClass") String seatClass);

    /**
     * Checks if a flight has any available seats in any class.
     *
     * @param flightId the flight ID
     * @return true if seats are available
     */
    @Query("SELECT CASE WHEN COUNT(sa) > 0 THEN true ELSE false END FROM DomainEntitySeatAvailability sa " +
           "WHERE sa.flightId = :flightId AND sa.availableSeats > 0")
    boolean hasAvailableSeats(@Param("flightId") UUID flightId);
}
