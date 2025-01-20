package ai.shreds.domain.services;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import ai.shreds.domain.entities.DomainEntityFlight;
import ai.shreds.domain.entities.DomainEntitySeatAvailability;
import ai.shreds.domain.exceptions.DomainFlightNotFoundException;
import ai.shreds.domain.exceptions.DomainSearchValidationException;
import ai.shreds.domain.ports.DomainPortFlightRepository;
import ai.shreds.domain.ports.DomainPortSeatAvailabilityRepository;
import ai.shreds.domain.ports.DomainPortFlightSearch;
import ai.shreds.domain.value_objects.DomainValueFlightSearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Domain service implementing flight search functionality.
 * Coordinates between repositories and enrichment service to provide complete flight information.
 */
@Slf4j
@Service
public class DomainServiceFlightSearch implements DomainPortFlightSearch {

    private final DomainPortFlightRepository flightRepository;
    private final DomainPortSeatAvailabilityRepository seatRepository;
    private final DomainServiceFlightEnrichment enrichmentService;

    public DomainServiceFlightSearch(DomainPortFlightRepository flightRepository,
                                    DomainPortSeatAvailabilityRepository seatRepository,
                                    DomainServiceFlightEnrichment enrichmentService) {
        this.flightRepository = Objects.requireNonNull(flightRepository, "Flight repository cannot be null");
        this.seatRepository = Objects.requireNonNull(seatRepository, "Seat repository cannot be null");
        this.enrichmentService = Objects.requireNonNull(enrichmentService, "Enrichment service cannot be null");
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainEntityFlight> searchFlights(DomainValueFlightSearchCriteria criteria) {
        try {
            log.debug("Searching flights with criteria: {}", criteria);

            // Retrieve base flight data
            List<DomainEntityFlight> flights = flightRepository.findFlightsByCriteria(criteria);
            
            if (flights.isEmpty()) {
                log.info("No flights found for criteria: {}", criteria);
                throw new DomainFlightNotFoundException("No flights found matching the search criteria");
            }

            // Collect all flight IDs
            List<UUID> flightIds = flights.stream()
                    .map(DomainEntityFlight::getFlightId)
                    .collect(Collectors.toList());

            // Retrieve seat availability data in bulk
            Map<UUID, List<DomainEntitySeatAvailability>> seatAvailabilityMap = retrieveSeatAvailability(flightIds);

            // Attach seat availability to flights
            attachSeatAvailability(flights, seatAvailabilityMap);

            // Filter by seat class if specified
            flights = filterBySeatClass(flights, criteria.getSeatClass());

            // Enrich with external data
            flights = enrichmentService.enrichFlightsWithExternalApi(flights);

            log.debug("Found {} matching flights", flights.size());
            return flights;

        } catch (DomainFlightNotFoundException | DomainSearchValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during flight search", e);
            throw new DomainSearchValidationException("Failed to process flight search: " + e.getMessage());
        }
    }

    private Map<UUID, List<DomainEntitySeatAvailability>> retrieveSeatAvailability(List<UUID> flightIds) {
        return flightIds.stream()
                .collect(Collectors.toMap(
                        flightId -> flightId,
                        seatRepository::findByFlightId
                ));
    }

    private void attachSeatAvailability(List<DomainEntityFlight> flights,
                                       Map<UUID, List<DomainEntitySeatAvailability>> seatAvailabilityMap) {
        flights.forEach(flight -> {
            List<DomainEntitySeatAvailability> availability = seatAvailabilityMap.get(flight.getFlightId());
            if (availability != null) {
                availability.forEach(flight::addSeatAvailability);
            }
        });
    }

    private List<DomainEntityFlight> filterBySeatClass(List<DomainEntityFlight> flights, String seatClass) {
        if (seatClass == null || seatClass.trim().isEmpty()) {
            return flights;
        }

        return flights.stream()
                .filter(flight -> flight.hasAvailabilityForClass(seatClass))
                .collect(Collectors.toList());
    }
}
