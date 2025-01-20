package ai.shreds.adapter.primary;

import ai.shreds.application.exceptions.ApplicationFlightSearchException;
import ai.shreds.application.exceptions.ApplicationInvalidSearchException;
import ai.shreds.application.ports.ApplicationFlightSearchPort;
import ai.shreds.shared.dtos.SharedFlightSearchRequest;
import ai.shreds.shared.dtos.SharedFlightSearchResponse;
import ai.shreds.adapter.exceptions.AdapterFlightSearchException;
import ai.shreds.adapter.exceptions.AdapterBadRequestException;
import ai.shreds.adapter.exceptions.AdapterNotFoundException;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller handling flight search operations.
 * Provides endpoints for searching flights with various criteria.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/flights")
public class AdapterFlightSearchController {

    private final ApplicationFlightSearchPort flightSearchPort;

    public AdapterFlightSearchController(ApplicationFlightSearchPort flightSearchPort) {
        this.flightSearchPort = flightSearchPort;
    }

    /**
     * Search for flights based on provided criteria.
     * @param request the search criteria
     * @param page optional page number (defaults to 0)
     * @param size optional page size (defaults to 20)
     * @return ResponseEntity containing the search results
     * @throws AdapterBadRequestException if the request is invalid
     * @throws AdapterNotFoundException if no flights are found
     * @throws AdapterFlightSearchException for other errors
     */
    @PostMapping("/search")
    public ResponseEntity<SharedFlightSearchResponse> searchFlights(
            @Valid @RequestBody SharedFlightSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.debug("Received flight search request: {}, page: {}, size: {}", request, page, size);
        
        try {
            validatePaginationParameters(page, size);
            
            SharedFlightSearchResponse response = flightSearchPort.searchFlights(request);
            
            // If pagination parameters are provided, create a paginated response
            if (page > 0 || size != 20) {
                response = response.createPaginatedResponse(page, size);
            }
            
            if (!response.hasResults()) {
                log.info("No flights found for search criteria: {}", request);
                throw new AdapterNotFoundException("No flights found matching the search criteria", "FLIGHTS_NOT_FOUND");
            }
            
            log.debug("Returning {} flights for search request", response.getTotalResults());
            return ResponseEntity.ok(response);
            
        } catch (ApplicationInvalidSearchException e) {
            log.warn("Invalid search request: {}", e.getMessage());
            throw new AdapterBadRequestException(e.getMessage(), "INVALID_SEARCH_REQUEST");
            
        } catch (ApplicationFlightSearchException e) {
            log.error("Flight search error: {}", e.getMessage());
            throw new AdapterNotFoundException(e.getMessage(), "FLIGHT_SEARCH_ERROR");
            
        } catch (Exception e) {
            log.error("Unexpected error during flight search", e);
            throw new AdapterFlightSearchException("An unexpected error occurred during flight search", "INTERNAL_ERROR");
        }
    }

    /**
     * Validates pagination parameters.
     * @param page page number
     * @param size page size
     * @throws AdapterBadRequestException if parameters are invalid
     */
    private void validatePaginationParameters(int page, int size) {
        if (page < 0) {
            throw new AdapterBadRequestException("Page number cannot be negative", "INVALID_PAGE");
        }
        if (size < 1 || size > 100) {
            throw new AdapterBadRequestException("Page size must be between 1 and 100", "INVALID_PAGE_SIZE");
        }
    }
}