package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityFlight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for flight entities.
 * Provides custom query methods for efficient flight searches.
 */
@Repository
public interface InfrastructureFlightJpaRepository extends JpaRepository<DomainEntityFlight, UUID> {

    /**
     * Finds flights by origin and destination within a date range.
     *
     * @param origin departure airport
     * @param destination arrival airport
     * @param fromDate start of date range
     * @param toDate end of date range
     * @return matching flights
     */
    @Query("SELECT f FROM DomainEntityFlight f " +
           "WHERE f.origin = :origin " +
           "AND f.destination = :destination " +
           "AND f.departureTime BETWEEN :fromDate AND :toDate " +
           "ORDER BY f.departureTime")
    List<DomainEntityFlight> findFlightsByRouteAndDateRange(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate);

    /**
     * Finds all flights for a specific airline.
     *
     * @param airlineId the airline's ID
     * @return list of flights
     */
    List<DomainEntityFlight> findByAirlineId(UUID airlineId);

    /**
     * Finds a flight by its number with eager loading of seat availability.
     *
     * @param flightId the flight ID
     * @return optional containing the flight if found
     */
    @Query("SELECT f FROM DomainEntityFlight f " +
           "LEFT JOIN FETCH f.seatAvailability " +
           "WHERE f.flightId = :flightId")
    Optional<DomainEntityFlight> findByIdWithSeatAvailability(@Param("flightId") UUID flightId);

    /**
     * Finds all flights departing from an airport within a time range.
     *
     * @param origin departure airport
     * @param fromTime start time
     * @param toTime end time
     * @return matching flights
     */
    @Query("SELECT f FROM DomainEntityFlight f " +
           "WHERE f.origin = :origin " +
           "AND f.departureTime BETWEEN :fromTime AND :toTime " +
           "ORDER BY f.departureTime")
    List<DomainEntityFlight> findDepartingFlights(
            @Param("origin") String origin,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime);

    /**
     * Finds all flights arriving at an airport within a time range.
     *
     * @param destination arrival airport
     * @param fromTime start time
     * @param toTime end time
     * @return matching flights
     */
    @Query("SELECT f FROM DomainEntityFlight f " +
           "WHERE f.destination = :destination " +
           "AND f.arrivalTime BETWEEN :fromTime AND :toTime " +
           "ORDER BY f.arrivalTime")
    List<DomainEntityFlight> findArrivingFlights(
            @Param("destination") String destination,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime);

    /**
     * Counts flights between two airports.
     *
     * @param origin departure airport
     * @param destination arrival airport
     * @return number of flights
     */
    @Query("SELECT COUNT(f) FROM DomainEntityFlight f " +
           "WHERE f.origin = :origin AND f.destination = :destination")
    long countFlightsByRoute(
            @Param("origin") String origin,
            @Param("destination") String destination);
}
