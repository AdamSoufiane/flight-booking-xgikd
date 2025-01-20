package ai.shreds.application.ports;

import ai.shreds.shared.value_objects.SharedRetrieveFlightSchedulesRequestParams;
import ai.shreds.shared.dtos.SharedRetrieveFlightSchedulesResponseDTO;
import ai.shreds.shared.dtos.SharedFlightScheduleResponseDTO;

import java.util.List;

/**
 * Input port for flight search operations.
 * Defines the contract for retrieving flight schedules based on various criteria.
 */
public interface ApplicationFlightSearchInputPort {

    /**
     * Retrieves flight schedules based on origin, destination, and date range.
     *
     * @param params Search parameters including origin, destination, and date range
     * @return DTO containing the matching flight schedules
     * @throws ai.shreds.application.exceptions.ApplicationException if the search operation fails
     */
    SharedRetrieveFlightSchedulesResponseDTO getFlightSchedules(SharedRetrieveFlightSchedulesRequestParams params);

    /**
     * Retrieves flight schedules for a specific airline.
     *
     * @param airlineId The ID of the airline
     * @param params Search parameters including origin, destination, and date range
     * @return DTO containing the matching flight schedules for the specified airline
     * @throws ai.shreds.application.exceptions.ApplicationException if the search operation fails
     */
    SharedRetrieveFlightSchedulesResponseDTO getAirlineFlightSchedules(String airlineId, 
            SharedRetrieveFlightSchedulesRequestParams params);

    /**
     * Retrieves details for a specific flight.
     *
     * @param flightId The unique identifier of the flight
     * @return Flight schedule details or null if not found
     * @throws ai.shreds.application.exceptions.ApplicationException if the retrieval fails
     */
    SharedFlightScheduleResponseDTO getFlightDetails(String flightId);

    /**
     * Searches for connecting flights between origin and destination.
     *
     * @param params Search parameters including origin, destination, and date range
     * @param maxConnections Maximum number of connections (default: 1)
     * @return DTO containing possible connecting flight combinations
     * @throws ai.shreds.application.exceptions.ApplicationException if the search operation fails
     */
    SharedRetrieveFlightSchedulesResponseDTO searchConnectingFlights(SharedRetrieveFlightSchedulesRequestParams params, 
            int maxConnections);
}
