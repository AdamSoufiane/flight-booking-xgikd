package ai.shreds.application.services;

import ai.shreds.application.exceptions.ApplicationException;
import ai.shreds.application.ports.ApplicationFlightSearchInputPort;
import ai.shreds.domain.entities.DomainEntityFlightSchedule;
import ai.shreds.domain.entities.DomainEntitySeatAvailability;
import ai.shreds.domain.ports.DomainPortFlightRepository;
import ai.shreds.domain.ports.DomainPortSeatAvailabilityRepository;
import ai.shreds.domain.services.DomainDataValidationService;
import ai.shreds.domain.services.DomainServiceFlightSearch;
import ai.shreds.shared.value_objects.SharedRetrieveFlightSchedulesRequestParams;
import ai.shreds.shared.dtos.SharedRetrieveFlightSchedulesResponseDTO;
import ai.shreds.shared.dtos.SharedFlightScheduleResponseDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service implementing flight search operations.
 */
@Service
@Transactional(readOnly = true)
public class ApplicationFlightSearchService implements ApplicationFlightSearchInputPort {

    private final DomainServiceFlightSearch domainServiceFlightSearch;
    private final DomainDataValidationService domainDataValidationService;
    private final DomainPortFlightRepository domainPortFlightRepository;
    private final DomainPortSeatAvailabilityRepository domainPortSeatAvailabilityRepository;

    public ApplicationFlightSearchService(
            DomainServiceFlightSearch domainServiceFlightSearch,
            DomainDataValidationService domainDataValidationService,
            DomainPortFlightRepository domainPortFlightRepository,
            DomainPortSeatAvailabilityRepository domainPortSeatAvailabilityRepository) {
        this.domainServiceFlightSearch = domainServiceFlightSearch;
        this.domainDataValidationService = domainDataValidationService;
        this.domainPortFlightRepository = domainPortFlightRepository;
        this.domainPortSeatAvailabilityRepository = domainPortSeatAvailabilityRepository;
    }

    @Override
    public SharedRetrieveFlightSchedulesResponseDTO getFlightSchedules(
            SharedRetrieveFlightSchedulesRequestParams params) {
        try {
            validateSearchParams(params);

            List<DomainEntityFlightSchedule> flights = domainServiceFlightSearch.searchFlights(
                    params.getOrigin(),
                    params.getDestination()
            );

            return createResponse(flights, params);
        } catch (Exception e) {
            throw new ApplicationException("Error retrieving flight schedules: " + e.getMessage(), e);
        }
    }

    @Override
    public SharedRetrieveFlightSchedulesResponseDTO getAirlineFlightSchedules(
            String airlineId, SharedRetrieveFlightSchedulesRequestParams params) {
        try {
            validateSearchParams(params);
            UUID airlineUuid = UUID.fromString(airlineId);

            List<DomainEntityFlightSchedule> flights = domainServiceFlightSearch.searchFlights(
                    params.getOrigin(),
                    params.getDestination()
            ).stream()
              .filter(flight -> flight.getAirlineId().equals(airlineUuid))
              .toList();

            return createResponse(flights, params);
        } catch (IllegalArgumentException e) {
            throw new ApplicationException("Invalid airline ID format: " + airlineId, e);
        } catch (Exception e) {
            throw new ApplicationException("Error retrieving airline flight schedules: " + e.getMessage(), e);
        }
    }

    @Override
    public SharedFlightScheduleResponseDTO getFlightDetails(String flightId) {
        try {
            UUID flightUuid = UUID.fromString(flightId);
            DomainEntityFlightSchedule flight = domainPortFlightRepository.findById(flightUuid);
            
            if (flight == null) {
                throw new ApplicationException("Flight not found: " + flightId);
            }

            List<DomainEntitySeatAvailability> seatAvailability = 
                    domainPortSeatAvailabilityRepository.findByFlightId(flightUuid);

            return mapToDetailedDTO(flight, seatAvailability);
        } catch (IllegalArgumentException e) {
            throw new ApplicationException("Invalid flight ID format: " + flightId, e);
        } catch (Exception e) {
            throw new ApplicationException("Error retrieving flight details: " + e.getMessage(), e);
        }
    }

    @Override
    public SharedRetrieveFlightSchedulesResponseDTO searchConnectingFlights(
            SharedRetrieveFlightSchedulesRequestParams params, int maxConnections) {
        try {
            validateSearchParams(params);
            if (maxConnections < 0 || maxConnections > 2) {
                throw new ApplicationException("Maximum connections must be between 0 and 2");
            }

            List<List<DomainEntityFlightSchedule>> connections = 
                    domainServiceFlightSearch.findConnectingFlights(
                            params.getOrigin(),
                            params.getDestination(),
                            maxConnections
                    );

            return createConnectionsResponse(connections, params);
        } catch (Exception e) {
            throw new ApplicationException("Error searching connecting flights: " + e.getMessage(), e);
        }
    }

    private void validateSearchParams(SharedRetrieveFlightSchedulesRequestParams params) {
        if (params == null) {
            throw new ApplicationException("Search parameters cannot be null");
        }
        if (!params.validateDifferentAirports()) {
            throw new ApplicationException("Origin and destination airports must be different");
        }
        if (!params.validateDateRange()) {
            throw new ApplicationException("Invalid date range format or logic");
        }
    }

    private SharedRetrieveFlightSchedulesResponseDTO createResponse(
            List<DomainEntityFlightSchedule> flights,
            SharedRetrieveFlightSchedulesRequestParams params) {
        
        List<SharedFlightScheduleResponseDTO> flightDtos = flights.stream()
                .map(this::mapToDTO)
                .toList();

        return SharedRetrieveFlightSchedulesResponseDTO.success(flightDtos, params);
    }

    private SharedRetrieveFlightSchedulesResponseDTO createConnectionsResponse(
            List<List<DomainEntityFlightSchedule>> connections,
            SharedRetrieveFlightSchedulesRequestParams params) {
        
        List<SharedFlightScheduleResponseDTO> allFlights = connections.stream()
                .flatMap(connection -> connection.stream().map(this::mapToDTO))
                .toList();

        return SharedRetrieveFlightSchedulesResponseDTO.builder()
                .flights(allFlights)
                .totalFlights(allFlights.size())
                .uniqueAirlines((int) allFlights.stream()
                        .map(SharedFlightScheduleResponseDTO::getAirlineId)
                        .distinct()
                        .count())
                .searchParams(params)
                .message(String.format("Found %d connecting flights", connections.size()))
                .build();
    }

    private SharedFlightScheduleResponseDTO mapToDTO(DomainEntityFlightSchedule flight) {
        return SharedFlightScheduleResponseDTO.builder()
                .flightId(flight.getFlightId().toString())
                .airlineId(flight.getAirlineId().toString())
                .flightNumber(flight.getFlightNumber())
                .departureTime(flight.getDepartureTime().toString())
                .arrivalTime(flight.getArrivalTime().toString())
                .origin(flight.getOrigin())
                .destination(flight.getDestination())
                .status(flight.getStatus())
                .aircraftType(flight.getAircraftType())
                .build();
    }

    private SharedFlightScheduleResponseDTO mapToDetailedDTO(
            DomainEntityFlightSchedule flight,
            List<DomainEntitySeatAvailability> seatAvailability) {
        
        SharedFlightScheduleResponseDTO dto = mapToDTO(flight);

        // Add seat availability information
        for (DomainEntitySeatAvailability seat : seatAvailability) {
            switch (seat.getSeatClass().toUpperCase()) {
                case "ECONOMY" -> dto.setBasicEconomyAvailable(seat.getAvailableSeats());
                case "BUSINESS" -> dto.setBusinessClassAvailable(seat.getAvailableSeats());
                case "FIRST" -> dto.setFirstClassAvailable(seat.getAvailableSeats());
            }
        }

        // Calculate duration
        long durationMinutes = java.time.Duration.between(
                flight.getDepartureTime(),
                flight.getArrivalTime()
        ).toMinutes();
        dto.setDurationMinutes((int) durationMinutes);

        return dto;
    }
}
