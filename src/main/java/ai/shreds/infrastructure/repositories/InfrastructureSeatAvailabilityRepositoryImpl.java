package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntitySeatAvailability;
import ai.shreds.domain.ports.DomainPortSeatAvailabilityRepository;
import ai.shreds.infrastructure.exceptions.InfrastructureDatabaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementation of the domain seat availability repository port.
 * Provides database access for seat availability operations with proper error handling.
 */
@Slf4j
@Repository
@Transactional(readOnly = true)
public class InfrastructureSeatAvailabilityRepositoryImpl implements DomainPortSeatAvailabilityRepository {

    private final InfrastructureSeatAvailabilityJpaRepository seatAvailabilityJpaRepository;

    public InfrastructureSeatAvailabilityRepositoryImpl(InfrastructureSeatAvailabilityJpaRepository seatAvailabilityJpaRepository) {
        this.seatAvailabilityJpaRepository = Objects.requireNonNull(seatAvailabilityJpaRepository,
                "Seat availability JPA repository cannot be null");
    }

    @Override
    public List<DomainEntitySeatAvailability> findByFlightId(UUID flightId) {
        try {
            log.debug("Fetching seat availability for flight: {}", flightId);

            if (flightId == null) {
                throw new IllegalArgumentException("Flight ID cannot be null");
            }

            List<DomainEntitySeatAvailability> availability = seatAvailabilityJpaRepository.findByFlightId(flightId);
            log.debug("Found {} seat availability records for flight {}", availability.size(), flightId);

            return availability;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching seat availability for flight {}", flightId, e);
            throw new InfrastructureDatabaseException(
                    "Failed to query seat availability: " + e.getMessage(),
                    "DB_QUERY_ERROR"
            );
        }
    }

    @Override
    @Transactional
    public DomainEntitySeatAvailability saveAvailability(DomainEntitySeatAvailability seatAvailability) {
        try {
            log.debug("Saving seat availability: {}", seatAvailability);

            validateSeatAvailability(seatAvailability);

            DomainEntitySeatAvailability saved = seatAvailabilityJpaRepository.save(seatAvailability);
            log.debug("Successfully saved seat availability with ID: {}", saved.getId());

            return saved;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error saving seat availability", e);
            throw new InfrastructureDatabaseException(
                    "Failed to save seat availability: " + e.getMessage(),
                    "DB_SAVE_ERROR"
            );
        }
    }

    /**
     * Finds seat availability for multiple flights in bulk.
     *
     * @param flightIds list of flight IDs
     * @return map of flight IDs to their seat availability
     */
    public List<DomainEntitySeatAvailability> findByFlightIds(List<UUID> flightIds) {
        try {
            log.debug("Fetching seat availability for {} flights", flightIds.size());

            if (flightIds == null || flightIds.isEmpty()) {
                return Collections.emptyList();
            }

            List<DomainEntitySeatAvailability> availability = seatAvailabilityJpaRepository.findByFlightIds(flightIds);
            log.debug("Found {} seat availability records", availability.size());

            return availability;

        } catch (Exception e) {
            log.error("Error fetching bulk seat availability", e);
            throw new InfrastructureDatabaseException(
                    "Failed to query bulk seat availability: " + e.getMessage(),
                    "DB_BULK_QUERY_ERROR"
            );
        }
    }

    /**
     * Updates seat availability with pessimistic locking.
     *
     * @param flightId the flight ID
     * @param seatClass the seat class
     * @param availability the new availability
     * @return updated seat availability
     */
    @Transactional
    public DomainEntitySeatAvailability updateAvailabilityWithLock(UUID flightId, String seatClass,
                                                                  DomainEntitySeatAvailability availability) {
        try {
            log.debug("Updating seat availability for flight {} and class {}", flightId, seatClass);

            validateSeatAvailability(availability);

            // Get with lock
            seatAvailabilityJpaRepository.findByFlightIdAndSeatClassForUpdate(flightId, seatClass)
                    .orElseThrow(() -> new IllegalStateException("Seat availability not found"));

            DomainEntitySeatAvailability updated = seatAvailabilityJpaRepository.save(availability);
            log.debug("Successfully updated seat availability with ID: {}", updated.getId());

            return updated;

        } catch (Exception e) {
            log.error("Error updating seat availability", e);
            throw new InfrastructureDatabaseException(
                    "Failed to update seat availability: " + e.getMessage(),
                    "DB_UPDATE_ERROR"
            );
        }
    }

    private void validateSeatAvailability(DomainEntitySeatAvailability seatAvailability) {
        if (seatAvailability == null) {
            throw new IllegalArgumentException("Seat availability cannot be null");
        }

        if (seatAvailability.getFlightId() == null) {
            throw new IllegalArgumentException("Flight ID is required");
        }

        if (seatAvailability.getSeatClass() == null || seatAvailability.getSeatClass().trim().isEmpty()) {
            throw new IllegalArgumentException("Seat class is required");
        }

        if (seatAvailability.getAvailableSeats() < 0) {
            throw new IllegalArgumentException("Available seats cannot be negative");
        }
    }
}
