package ai.shreds.application.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ai.shreds.shared.dtos.SharedFlightDTO;
import ai.shreds.shared.dtos.SharedFlightSearchRequest;
import ai.shreds.shared.dtos.SharedFlightSearchResponse;
import ai.shreds.shared.dtos.SharedSeatAvailabilityDTO;
import ai.shreds.domain.ports.DomainPortCache;
import ai.shreds.domain.entities.DomainEntityFlight;
import ai.shreds.domain.entities.DomainEntitySeatAvailability;
import ai.shreds.domain.value_objects.DomainValueFlightSearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service handling caching operations for flight searches.
 * Implements caching strategy with TTL and mapping between domain and shared DTOs.
 */
@Slf4j
@Service
public class ApplicationCacheService {

    private final DomainPortCache domainPortCache;

    public ApplicationCacheService(DomainPortCache domainPortCache) {
        this.domainPortCache = domainPortCache;
    }

    /**
     * Retrieves cached search results if available.
     *
     * @param request the search request
     * @return Optional containing cached response if available
     */
    public Optional<SharedFlightSearchResponse> retrieveCachedSearch(SharedFlightSearchRequest request) {
        log.debug("Attempting to retrieve cached results for search request: {}", request);
        
        DomainValueFlightSearchCriteria criteria = mapToDomainCriteria(request);
        
        return domainPortCache.retrieveCachedSearch(criteria)
                .map(flights -> {
                    log.debug("Cache hit. Found {} flights in cache", flights.size());
                    return mapDomainFlightsToSearchResponse(flights);
                });
    }

    /**
     * Caches search results for future retrieval.
     *
     * @param request the original search request
     * @param response the search response to cache
     */
    public void cacheSearchResults(SharedFlightSearchRequest request, SharedFlightSearchResponse response) {
        log.debug("Caching search results for request: {}", request);
        
        DomainValueFlightSearchCriteria criteria = mapToDomainCriteria(request);
        List<DomainEntityFlight> flights = mapSearchResponseToDomainFlights(response);
        
        domainPortCache.cacheSearchResults(criteria, flights);
        log.debug("Successfully cached {} flights", flights.size());
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

    private SharedFlightSearchResponse mapDomainFlightsToSearchResponse(List<DomainEntityFlight> flights) {
        List<SharedFlightDTO> flightDTOs = flights.stream()
                .map(this::mapDomainFlightToDTO)
                .collect(Collectors.toList());

        return SharedFlightSearchResponse.builder()
                .flightList(flightDTOs)
                .totalResults(flightDTOs.size())
                .searchTimestamp(LocalDateTime.now())
                .build();
    }

    private SharedFlightDTO mapDomainFlightToDTO(DomainEntityFlight flight) {
        return SharedFlightDTO.builder()
                .flightId(flight.getFlightId())
                .airlineId(flight.getAirlineId())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .origin(flight.getOrigin())
                .destination(flight.getDestination())
                .seatAvailability(mapDomainSeatAvailability(flight.getSeatAvailability()))
                .build();
    }

    private List<SharedSeatAvailabilityDTO> mapDomainSeatAvailability(List<DomainEntitySeatAvailability> availability) {
        return availability.stream()
                .map(seat -> SharedSeatAvailabilityDTO.builder()
                        .seatClass(seat.getSeatClass())
                        .availableSeats(seat.getAvailableSeats())
                        .build())
                .collect(Collectors.toList());
    }

    private List<DomainEntityFlight> mapSearchResponseToDomainFlights(SharedFlightSearchResponse response) {
        return response.getFlightList().stream()
                .map(this::mapDTOToDomainFlight)
                .collect(Collectors.toList());
    }

    private DomainEntityFlight mapDTOToDomainFlight(SharedFlightDTO dto) {
        return DomainEntityFlight.builder()
                .flightId(dto.getFlightId())
                .airlineId(dto.getAirlineId())
                .departureTime(dto.getDepartureTime())
                .arrivalTime(dto.getArrivalTime())
                .origin(dto.getOrigin())
                .destination(dto.getDestination())
                .seatAvailability(mapDTOSeatAvailability(dto.getSeatAvailability()))
                .build();
    }

    private List<DomainEntitySeatAvailability> mapDTOSeatAvailability(List<SharedSeatAvailabilityDTO> availability) {
        return availability.stream()
                .map(seat -> DomainEntitySeatAvailability.builder()
                        .seatClass(seat.getSeatClass())
                        .availableSeats(seat.getAvailableSeats())
                        .build())
                .collect(Collectors.toList());
    }
}
