package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityFlightSchedule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Port interface for flight schedule repository operations in the domain layer.
 */
public interface DomainPortFlightRepository {

    /**
     * Creates a new flight schedule.
     *
     * @param entity Flight schedule to create
     * @throws ai.shreds.domain.exceptions.DomainException if creation fails
     */
    void createFlightSchedule(DomainEntityFlightSchedule entity);

    /**
     * Updates an existing flight schedule.
     *
     * @param entity Flight schedule to update
     * @throws ai.shreds.domain.exceptions.DomainException if update fails
     */
    void updateFlightSchedule(DomainEntityFlightSchedule entity);

    /**
     * Finds a flight schedule by its string ID.
     *
     * @param flightId Flight schedule ID
     * @return Found flight schedule or null
     */
    DomainEntityFlightSchedule findFlightScheduleById(String flightId);

    /**
     * Retrieves all flight schedules.
     *
     * @return List of all flight schedules
     */
    List<DomainEntityFlightSchedule> findAllFlightSchedules();

    /**
     * Saves a flight schedule (creates or updates).
     *
     * @param flight Flight schedule to save
     * @return Saved flight schedule
     */
    DomainEntityFlightSchedule save(DomainEntityFlightSchedule flight);

    /**
     * Finds a flight schedule by its UUID.
     *
     * @param id Flight schedule UUID
     * @return Found flight schedule or null
     */
    DomainEntityFlightSchedule findById(UUID id);

    /**
     * Finds flight schedules by origin and destination.
     *
     * @param origin Origin airport code
     * @param destination Destination airport code
     * @return List of matching flight schedules
     */
    List<DomainEntityFlightSchedule> findByOriginAndDestination(String origin, String destination);

    /**
     * Finds flight schedules by airline ID.
     *
     * @param airlineId Airline ID
     * @return List of matching flight schedules
     */
    List<DomainEntityFlightSchedule> findByAirlineId(UUID airlineId);

    /**
     * Finds flight schedules by aircraft ID.
     *
     * @param aircraftId Aircraft ID
     * @return List of matching flight schedules
     */
    List<DomainEntityFlightSchedule> findByAircraftId(UUID aircraftId);

    /**
     * Finds flight schedules within a date range.
     *
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return List of matching flight schedules
     */
    List<DomainEntityFlightSchedule> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds flight schedules by airline and date range.
     *
     * @param airlineId Airline ID
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return List of matching flight schedules
     */
    List<DomainEntityFlightSchedule> findByAirlineAndDateRange(
            UUID airlineId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds connecting flights between origin and destination.
     *
     * @param origin Origin airport code
     * @param destination Destination airport code
     * @param maxConnections Maximum number of connections
     * @return List of lists, where each inner list represents a connection combination
     */
    List<List<DomainEntityFlightSchedule>> findConnectingFlights(
            String origin, String destination, int maxConnections);

    /**
     * Deletes old flight schedules.
     *
     * @param cutoffDate Schedules before this date will be deleted
     * @return Number of deleted schedules
     */
    int deleteOldFlights(LocalDateTime cutoffDate);

    /**
     * Counts flights by airline.
     *
     * @param airlineId Airline ID
     * @return Number of flights for the airline
     */
    long countByAirline(UUID airlineId);

    /**
     * Checks if a flight exists.
     *
     * @param flightId Flight ID
     * @return true if flight exists
     */
    boolean existsById(UUID flightId);
}
