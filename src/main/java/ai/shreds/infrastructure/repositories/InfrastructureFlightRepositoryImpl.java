package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityFlightSchedule;
import ai.shreds.domain.ports.DomainPortFlightRepository;
import ai.shreds.infrastructure.exceptions.InfrastructureException;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

/**
 * Implementation of the flight repository interface.
 * Handles persistence operations for flight schedules with caching support.
 */
@Repository
@Transactional
public class InfrastructureFlightRepositoryImpl implements DomainPortFlightRepository {

    private static final String CACHE_NAME = "flightSchedules";
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void createFlightSchedule(DomainEntityFlightSchedule entity) {
        try {
            entity.setLastUpdated(LocalDateTime.now());
            entityManager.persist(entity);
        } catch (Exception e) {
            throw new InfrastructureException("Error creating flight schedule", e);
        }
    }

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void updateFlightSchedule(DomainEntityFlightSchedule entity) {
        try {
            entity.setLastUpdated(LocalDateTime.now());
            entityManager.merge(entity);
        } catch (Exception e) {
            throw new InfrastructureException("Error updating flight schedule", e);
        }
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "#flightId")
    public DomainEntityFlightSchedule findFlightScheduleById(String flightId) {
        try {
            UUID uuid = UUID.fromString(flightId);
            return entityManager.find(DomainEntityFlightSchedule.class, uuid);
        } catch (IllegalArgumentException e) {
            throw new InfrastructureException("Invalid flight ID format: " + flightId, e);
        } catch (Exception e) {
            throw new InfrastructureException("Error finding flight schedule by ID", e);
        }
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "'all'")
    public List<DomainEntityFlightSchedule> findAllFlightSchedules() {
        try {
            TypedQuery<DomainEntityFlightSchedule> query = entityManager.createQuery(
                    "SELECT fs FROM DomainEntityFlightSchedule fs ORDER BY fs.departureTime",
                    DomainEntityFlightSchedule.class);
            return query.getResultList();
        } catch (Exception e) {
            throw new InfrastructureException("Error finding all flight schedules", e);
        }
    }

    @Override
    @CachePut(value = CACHE_NAME, key = "#flight.flightId")
    public DomainEntityFlightSchedule save(DomainEntityFlightSchedule flight) {
        try {
            flight.setLastUpdated(LocalDateTime.now());
            if (flight.getFlightId() == null) {
                entityManager.persist(flight);
                return flight;
            } else {
                return entityManager.merge(flight);
            }
        } catch (Exception e) {
            throw new InfrastructureException("Error saving flight schedule", e);
        }
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "#id")
    public DomainEntityFlightSchedule findById(UUID id) {
        try {
            return entityManager.find(DomainEntityFlightSchedule.class, id);
        } catch (Exception e) {
            throw new InfrastructureException("Error finding flight schedule by UUID", e);
        }
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "#origin + '-' + #destination")
    public List<DomainEntityFlightSchedule> findByOriginAndDestination(String origin, String destination) {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<DomainEntityFlightSchedule> cq = cb.createQuery(DomainEntityFlightSchedule.class);
            Root<DomainEntityFlightSchedule> flight = cq.from(DomainEntityFlightSchedule.class);

            List<Predicate> predicates = new ArrayList<>();
            if (origin != null) {
                predicates.add(cb.equal(flight.get("origin"), origin));
            }
            if (destination != null) {
                predicates.add(cb.equal(flight.get("destination"), destination));
            }

            cq.where(predicates.toArray(new Predicate[0]));
            cq.orderBy(cb.asc(flight.get("departureTime")));

            return entityManager.createQuery(cq).getResultList();
        } catch (Exception e) {
            throw new InfrastructureException("Error finding flight schedules by origin and destination", e);
        }
    }

    /**
     * Finds all flights for a specific aircraft.
     */
    @Cacheable(value = CACHE_NAME, key = "'aircraft-' + #aircraftId")
    public List<DomainEntityFlightSchedule> findByAircraftId(UUID aircraftId) {
        try {
            TypedQuery<DomainEntityFlightSchedule> query = entityManager.createQuery(
                    "SELECT fs FROM DomainEntityFlightSchedule fs WHERE fs.aircraftId = :aircraftId " +
                    "ORDER BY fs.departureTime",
                    DomainEntityFlightSchedule.class);
            query.setParameter("aircraftId", aircraftId);
            return query.getResultList();
        } catch (Exception e) {
            throw new InfrastructureException("Error finding flights by aircraft ID", e);
        }
    }

    /**
     * Finds all flights for a specific airline within a date range.
     */
    @Cacheable(value = CACHE_NAME, key = "'airline-' + #airlineId + '-' + #startDate + '-' + #endDate")
    public List<DomainEntityFlightSchedule> findByAirlineAndDateRange(
            UUID airlineId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            TypedQuery<DomainEntityFlightSchedule> query = entityManager.createQuery(
                    "SELECT fs FROM DomainEntityFlightSchedule fs " +
                    "WHERE fs.airlineId = :airlineId " +
                    "AND fs.departureTime BETWEEN :startDate AND :endDate " +
                    "ORDER BY fs.departureTime",
                    DomainEntityFlightSchedule.class);
            query.setParameter("airlineId", airlineId);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            return query.getResultList();
        } catch (Exception e) {
            throw new InfrastructureException("Error finding flights by airline and date range", e);
        }
    }

    /**
     * Deletes old flight schedules.
     */
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void deleteOldFlights(LocalDateTime cutoffDate) {
        try {
            entityManager.createQuery(
                    "DELETE FROM DomainEntityFlightSchedule fs " +
                    "WHERE fs.departureTime < :cutoffDate")
                    .setParameter("cutoffDate", cutoffDate)
                    .executeUpdate();
        } catch (Exception e) {
            throw new InfrastructureException("Error deleting old flights", e);
        }
    }
}
