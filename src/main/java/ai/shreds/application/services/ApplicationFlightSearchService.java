package ai.shreds.application.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import ai.shreds.application.exceptions.ApplicationFlightSearchException;
import ai.shreds.application.ports.ApplicationFlightSearchPort;
import ai.shreds.domain.ports.DomainPortFlightSearch;
import ai.shreds.domain.value_objects.DomainValueFlightSearchCriteria;
import ai.shreds.domain.entities.DomainEntityFlight;
import ai.shreds.shared.dtos.SharedFlightSearchRequest;
import ai.shreds.shared.dtos.SharedFlightSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Implementation of the flight search application port.
 * Coordinates between validation, caching, and domain services.
 */
@Slf4j
@Service
public class ApplicationFlightSearchService implements ApplicationFlightSearchPort {

    private final ApplicationCacheService cacheService;
    private final ApplicationSearchValidationService validationService;
    private final DomainPortFlightSearch domainFlightSearch;

    public ApplicationFlightSearchService(
            ApplicationCacheService cacheService,
            ApplicationSearchValidationService validationService,
            DomainPortFlightSearch domainFlightSearch) {
        this.cacheService = cacheService;
        this.validationService = validationService;
        this.domainFlightSearch = domainFlightSearch;
    }

    @Override
    public SharedFlightSearchResponse searchFlights(SharedFlightSearchRequest request) {
        log.debug("Processing flight search request: {}", request);
        
        try {
            // Validate request parameters
            validationService.validateSearchParameters(request);

            // Check cache first
            Optional<SharedFlightSearchResponse> cachedResponse = cacheService.retrieveCachedSearch(request);
            if (cachedResponse.isPresent()) {
                log.debug("Returning cached search results");
                return cachedResponse.get();
            }

            // Perform domain search
            List<DomainEntityFlight> domainFlights = domainFlightSearch.searchFlights(mapToDomainCriteria(request));
            
            // Convert and prepare response
            SharedFlightSearchResponse response = createSearchResponse(domainFlights);
            
            // Cache the results asynchronously
            CompletableFuture.runAsync(() -> cacheService.cacheSearchResults(request, response));

            log.debug("Returning {} flights from fresh search", response.getTotalResults());
            return response;

        } catch (Exception e) {
            log.error("Error during flight search", e);
            throw new ApplicationFlightSearchException("Failed to process flight search: " + e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void refreshSearchCache(SharedFlightSearchRequest request) {
        log.debug("Refreshing cache for search request: {}", request);
        try {
            validationService.validateSearchParameters(request);
            List<DomainEntityFlight> freshResults = domainFlightSearch.searchFlights(mapToDomainCriteria(request));
            SharedFlightSearchResponse response = createSearchResponse(freshResults);
            cacheService.cacheSearchResults(request, response);
            log.debug("Cache refresh completed successfully");
        } catch (Exception e) {
            log.error("Failed to refresh cache", e);
            throw new ApplicationFlightSearchException("Cache refresh failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void clearSearchCache(SharedFlightSearchRequest request) {
        log.debug("Clearing cache for search request: {}", request);
        try {
            cacheService.cacheSearchResults(request, new SharedFlightSearchResponse());
            log.debug("Cache cleared successfully");
        } catch (Exception e) {
            log.error("Failed to clear cache", e);
            throw new ApplicationFlightSearchException("Cache clear failed: " + e.getMessage(), e);
        }
    }

    private DomainValueFlightSearchCriteria mapToDomainCriteria(SharedFlightSearchRequest request) {
        return DomainValueFlightSearchCriteria.builder()
                .origin(request.getOrigin().toUpperCase())
                .destination(request.getDestination().toUpperCase())
                .departureDate(request.getDepartureDate())
                .returnDate(request.getReturnDate())
                .seatClass(request.getSeatClass())
                .build();
    }

    private SharedFlightSearchResponse createSearchResponse(List<DomainEntityFlight> flights) {
        return SharedFlightSearchResponse.builder()
                .flightList(flights.stream()
                        .map(DomainEntityFlight::toSharedFlightDTO)
                        .toList())
                .totalResults(flights.size())
                .searchTimestamp(LocalDateTime.now())
                .build();
    }
}
