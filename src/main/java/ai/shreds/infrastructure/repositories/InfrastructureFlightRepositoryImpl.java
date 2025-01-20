package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityFlight;
import ai.shreds.domain.value_objects.DomainValueFlightSearchCriteria;
import ai.shreds.domain.ports.DomainPortFlightRepository;
import ai.shreds.infrastructure.exceptions.InfrastructureDatabaseException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementation of the domain flight repository port.
 * Provides database access for flight-related operations.
 */
@Slf4j
@Repository
@Transactional(readOnly = true)
public class InfrastructureFlightRepositoryImpl implements DomainPortFlightRepository {

    private final InfrastructureFlightJpaRepository flightJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final int MAX_SEARCH_WINDOW_DAYS = 7;

    public InfrastructureFlightRepositoryImpl(InfrastructureFlightJpaRepository flightJpaRepository) {
        this.flightJpaRepository = Objects.requireNonNull(flightJpaRepository, "Flight JPA repository cannot be null");
    }

    @Override
    public List<DomainEntityFlight> findFlightsByCriteria(DomainValueFlightSearchCriteria criteria) {
        try {
            log.debug("Searching flights with criteria: {}", criteria);

            validateSearchCriteria(criteria);

            // Calculate search window
            LocalDateTime searchWindowEnd = calculateSearchWindowEnd(criteria);

            // Use the new repository method
            List<DomainEntityFlight> flights = flightJpaRepository.findFlightsByRouteAndDateRange(
                    criteria.getOrigin(),
                    criteria.getDestination(),
                    criteria.getDepartureDate(),
                    searchWindowEnd
            );

            log.debug("Found {} flights matching criteria", flights.size());
            return flights;

        } catch (InfrastructureDatabaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error searching flights", e);
            throw new InfrastructureDatabaseException(
                    "Failed to search flights: " + e.getMessage(),
                    "DB_SEARCH_ERROR"
            );
        }
    }

    @Override
    @Transactional
    public DomainEntityFlight saveFlight(DomainEntityFlight flight) {
        try {
            log.debug("Saving flight: {}", flight);

            if (flight == null) {
                throw new IllegalArgumentException("Flight cannot be null");
            }

            flight.validate();

            DomainEntityFlight savedFlight = flightJpaRepository.save(flight);
            log.debug("Successfully saved flight with ID: {}", savedFlight.getFlightId());
            
            return savedFlight;

        } catch (Exception e) {
            log.error("Error saving flight", e);
            throw new InfrastructureDatabaseException(
                    "Failed to save flight: " + e.getMessage(),
                    "DB_SAVE_ERROR"
            );
        }
    }

    private void validateSearchCriteria(DomainValueFlightSearchCriteria criteria) {
        if (criteria == null) {
            throw new IllegalArgumentException("Search criteria cannot be null");
        }

        if (criteria.getOrigin() == null || criteria.getOrigin().trim().isEmpty()) {
            throw new IllegalArgumentException("Origin is required");
        }

        if (criteria.getDestination() == null || criteria.getDestination().trim().isEmpty()) {
            throw new IllegalArgumentException("Destination is required");
        }

        if (criteria.getDepartureDate() == null) {
            throw new IllegalArgumentException("Departure date is required");
        }
    }

    private LocalDateTime calculateSearchWindowEnd(DomainValueFlightSearchCriteria criteria) {
        if (criteria.getReturnDate() != null) {
            return criteria.getReturnDate();
        }

        // If no return date specified, search up to MAX_SEARCH_WINDOW_DAYS from departure
        return criteria.getDepartureDate()
                .plusDays(MAX_SEARCH_WINDOW_DAYS)
                .truncatedTo(ChronoUnit.DAYS);
    }
}
