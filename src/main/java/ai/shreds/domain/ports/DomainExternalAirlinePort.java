package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityFlightSchedule;
import ai.shreds.domain.entities.DomainEntityAirlineInfo;
import ai.shreds.domain.value_objects.DomainValueSeatAvailability;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Port interface for external airline API interactions.
 */
public interface DomainExternalAirlinePort {

    /**
     * Makes a generic call to the airline API.
     *
     * @param endpoint API endpoint to call
     * @param method HTTP method
     * @param params Request parameters
     * @param headers Request headers
     * @return API response
     * @throws ai.shreds.domain.exceptions.DomainException if API call fails
     */
    Map<String, Object> callAirlineAPI(String endpoint, String method, 
            Map<String, Object> params, Map<String, String> headers);

    /**
     * Fetches flight data for specific airlines.
     *
     * @param airlineIds List of airline IDs
     * @return List of flight schedules
     * @throws ai.shreds.domain.exceptions.DomainException if fetch fails
     */
    List<DomainEntityFlightSchedule> fetchFlightData(List<UUID> airlineIds);

    /**
     * Fetches flight schedules for a specific date range.
     *
     * @param airlineId Airline ID
     * @param startDate Start of date range
     * @param endDate End of date range
     * @return List of flight schedules
     */
    List<DomainEntityFlightSchedule> fetchFlightSchedules(
            UUID airlineId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Fetches seat availability for specific flights.
     *
     * @param flightIds List of flight IDs
     * @return List of seat availability records
     */
    List<DomainValueSeatAvailability> fetchSeatAvailability(List<UUID> flightIds);

    /**
     * Fetches airline information.
     *
     * @param airlineId Airline ID
     * @return Airline information
     */
    DomainEntityAirlineInfo fetchAirlineInfo(UUID airlineId);

    /**
     * Checks API health status.
     *
     * @param airlineId Airline ID
     * @return true if API is healthy
     */
    boolean checkApiHealth(UUID airlineId);

    /**
     * Gets API rate limit status.
     *
     * @param airlineId Airline ID
     * @return Map containing rate limit information
     */
    Map<String, Object> getRateLimitStatus(UUID airlineId);

    /**
     * Authenticates with the airline API.
     *
     * @param airlineId Airline ID
     * @param credentials API credentials
     * @return Authentication token or session information
     */
    Map<String, String> authenticate(UUID airlineId, Map<String, String> credentials);

    /**
     * Refreshes API authentication token.
     *
     * @param airlineId Airline ID
     * @param currentToken Current authentication token
     * @return New authentication token
     */
    String refreshToken(UUID airlineId, String currentToken);

    /**
     * Validates API response data.
     *
     * @param response API response data
     * @return List of validation errors, empty if valid
     */
    List<String> validateApiResponse(Map<String, Object> response);
}
