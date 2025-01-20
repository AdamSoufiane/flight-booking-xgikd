package ai.shreds.domain.ports;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import ai.shreds.domain.entities.DomainEntityAirlineInfo;
import ai.shreds.domain.entities.DomainEntityFlightSchedule;
import ai.shreds.domain.value_objects.DomainValueFlightSchedule;
import ai.shreds.domain.value_objects.DomainValueSeatAvailability;

/**
 * Port interface for synchronizing flight data from external sources.
 */
public interface DomainPortFlightDataSync {

    /**
     * Fetches flight schedule data from external sources.
     *
     * @return List of flight schedules
     * @throws ai.shreds.domain.exceptions.DomainException if fetch fails
     */
    List<DomainValueFlightSchedule> fetchExternalFlightData();

    /**
     * Fetches seat availability data from external sources.
     *
     * @return List of seat availability records
     * @throws ai.shreds.domain.exceptions.DomainException if fetch fails
     */
    List<DomainValueSeatAvailability> fetchExternalSeatData();

    /**
     * Fetches airline information from external sources.
     *
     * @return List of airline information records
     * @throws ai.shreds.domain.exceptions.DomainException if fetch fails
     */
    List<DomainEntityAirlineInfo> fetchExternalAirlineData();

    /**
     * Fetches flight data for specific airlines.
     *
     * @param airlineIds List of airline IDs
     * @return List of flight schedules
     * @throws ai.shreds.domain.exceptions.DomainException if fetch fails
     */
    List<DomainEntityFlightSchedule> fetchFlightData(List<UUID> airlineIds);

    /**
     * Fetches flight data for a specific date range.
     *
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return List of flight schedules
     */
    List<DomainEntityFlightSchedule> fetchFlightDataByDateRange(
            LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Fetches flight data for specific airlines and date range.
     *
     * @param airlineIds List of airline IDs
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return List of flight schedules
     */
    List<DomainEntityFlightSchedule> fetchFlightDataByAirlineAndDateRange(
            List<UUID> airlineIds, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Fetches seat availability for specific flights.
     *
     * @param flightIds List of flight IDs
     * @return List of seat availability records
     */
    List<DomainValueSeatAvailability> fetchSeatDataByFlights(List<UUID> flightIds);

    /**
     * Fetches airline information for specific airlines.
     *
     * @param airlineIds List of airline IDs
     * @return List of airline information records
     */
    List<DomainEntityAirlineInfo> fetchAirlineDataByIds(List<UUID> airlineIds);

    /**
     * Checks if external data sources are available.
     *
     * @return true if all data sources are available
     */
    boolean checkDataSourceAvailability();

    /**
     * Gets the last successful sync time for each data type.
     *
     * @return Map of data type to last sync time
     */
    java.util.Map<String, LocalDateTime> getLastSyncTimes();

    /**
     * Validates external data before ingestion.
     *
     * @param flights List of flight schedules to validate
     * @return List of validation errors, empty if all valid
     */
    List<String> validateExternalData(List<DomainEntityFlightSchedule> flights);
}
