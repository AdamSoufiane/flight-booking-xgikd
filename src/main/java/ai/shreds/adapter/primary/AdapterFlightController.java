package ai.shreds.adapter.primary;

import ai.shreds.application.ports.ApplicationFlightIngestionInputPort;
import ai.shreds.application.ports.ApplicationFlightSearchInputPort;
import ai.shreds.shared.value_objects.SharedIngestionRequestParams;
import ai.shreds.shared.dtos.SharedIngestionResponseDTO;
import ai.shreds.shared.value_objects.SharedRetrieveFlightSchedulesRequestParams;
import ai.shreds.shared.dtos.SharedRetrieveFlightSchedulesResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

/**
 * REST Controller for flight-related operations.
 * Handles flight data ingestion and flight schedule retrieval requests.
 */
@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Validated
@Tag(name = "Flight Operations", description = "APIs for flight data ingestion and retrieval")
public class AdapterFlightController {

    private final ApplicationFlightIngestionInputPort applicationFlightIngestionInputPort;
    private final ApplicationFlightSearchInputPort applicationFlightSearchInputPort;

    /**
     * Initiates the ingestion of flight data for specified airlines.
     *
     * @param params The ingestion request parameters containing airline IDs and date range
     * @return ResponseEntity containing the ingestion operation result
     */
    @PostMapping("/ingest")
    @Operation(summary = "Ingest flight data", 
              description = "Initiates the ingestion of flight data for specified airlines")
    public ResponseEntity<SharedIngestionResponseDTO> ingestFlightData(
            @Valid @RequestBody SharedIngestionRequestParams params) {
        SharedIngestionResponseDTO response = applicationFlightIngestionInputPort.ingestFlightData(params);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves flight schedules based on search criteria.
     *
     * @param origin IATA/ICAO code of the origin airport
     * @param destination IATA/ICAO code of the destination airport
     * @param dateRange Date range in format YYYY-MM-DD/YYYY-MM-DD
     * @return ResponseEntity containing the matching flight schedules
     */
    @GetMapping
    @Operation(summary = "Search flight schedules", 
              description = "Retrieves flight schedules based on origin, destination, and date range")
    public ResponseEntity<SharedRetrieveFlightSchedulesResponseDTO> getFlightSchedules(
            @Pattern(regexp = "^[A-Z]{3,4}$", message = "Origin must be a valid IATA/ICAO code")
            @RequestParam String origin,
            
            @Pattern(regexp = "^[A-Z]{3,4}$", message = "Destination must be a valid IATA/ICAO code")
            @RequestParam String destination,
            
            @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}/\\d{4}-\\d{2}-\\d{2}", 
                    message = "Date range must be in format YYYY-MM-DD/YYYY-MM-DD")
            @RequestParam String dateRange) {

        SharedRetrieveFlightSchedulesRequestParams params = SharedRetrieveFlightSchedulesRequestParams.builder()
                .origin(origin)
                .destination(destination)
                .dateRange(dateRange)
                .build();

        if (!params.validateDifferentAirports()) {
            return ResponseEntity.badRequest().body(
                SharedRetrieveFlightSchedulesResponseDTO.error("Origin and destination airports must be different"));
        }

        if (!params.validateDateRange()) {
            return ResponseEntity.badRequest().body(
                SharedRetrieveFlightSchedulesResponseDTO.error("Invalid date range format or logic"));
        }

        SharedRetrieveFlightSchedulesResponseDTO response = applicationFlightSearchInputPort.getFlightSchedules(params);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the status of a specific ingestion operation.
     *
     * @param ingestionId The ID of the ingestion operation
     * @return ResponseEntity containing the current status of the ingestion operation
     */
    @GetMapping("/ingest/{ingestionId}/status")
    @Operation(summary = "Get ingestion status", 
              description = "Retrieves the status of a specific ingestion operation")
    public ResponseEntity<SharedIngestionResponseDTO> getIngestionStatus(
            @PathVariable String ingestionId) {
        SharedIngestionResponseDTO status = applicationFlightIngestionInputPort.getIngestionStatus(ingestionId);
        return ResponseEntity.ok(status);
    }
}