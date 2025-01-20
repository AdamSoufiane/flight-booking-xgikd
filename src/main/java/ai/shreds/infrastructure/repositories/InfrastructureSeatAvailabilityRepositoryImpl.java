package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntitySeatAvailability;
import ai.shreds.domain.ports.DomainPortSeatAvailabilityRepository;
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

/**
 * Implementation of the seat availability repository interface.
 * Handles persistence operations for seat availability with caching support.
 */
@Repository
@Transactional
public class InfrastructureSeatAvailabilityRepositoryImpl implements DomainPortSeatAvailabilityRepository {

    private static final String CACHE_NAME = "seatAvailability";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @CacheEvict(value = CACHE_NAME, key = "#entity.flightId")
    public void createSeatAvailability(DomainEntitySeatAvailability entity) {
        try {
            entity.setLastUpdated(LocalDateTime.now());
            entityManager.persist(entity);
        } catch (Exception e) {
            throw new InfrastructureException("Error creating seat availability", e);
        }
    }

    @Override
    @CacheEvict(value = CACHE_NAME, key = "#entity.flightId")
    public void updateSeatAvailability(DomainEntitySeatAvailability entity) {
        try {
            entity.setLastUpdated(LocalDateTime.now());
            entityManager.merge(entity);
        } catch (Exception e) {
            throw new InfrastructureException("Error updating seat availability", e);
        }
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "#flightId")
    public List<DomainEntitySeatAvailability> findSeatAvailabilityByFlightId(String flightId) {
        try {
            UUID uuid = UUID.fromString(flightId);
            return findByFlightId(uuid);
        } catch (IllegalArgumentException e) {
            throw new InfrastructureException("Invalid flight ID format: " + flightId, e);
        } catch (Exception e) {
            throw new InfrastructureException("Error finding seat availability", e);
        }
    }

    @Override
    @CachePut(value = CACHE_NAME, key = "#seatAvailability.flightId")
    public DomainEntitySeatAvailability save(DomainEntitySeatAvailability seatAvailability) {
        try {
            seatAvailability.setLastUpdated(LocalDateTime.now());
            if (seatAvailability.getId() == null) {
                entityManager.persist(seatAvailability);
                return seatAvailability;
            } else {
                return entityManager.merge(seatAvailability);
            }
        } catch (Exception e) {
            throw new InfrastructureException("Error saving seat availability", e);
        }
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "#flightId")
    public List<DomainEntitySeatAvailability> findByFlightId(UUID flightId) {
        try {
            TypedQuery<DomainEntitySeatAvailability> query = entityManager.createQuery(
                    "SELECT sa FROM DomainEntitySeatAvailability sa " +
                    "WHERE sa.flightId = :flightId " +
                    "ORDER BY sa.seatClass",
                    DomainEntitySeatAvailability.class);
            query.setParameter("flightId", flightId);
            return query.getResultList();
        } catch (Exception e) {
            throw new InfrastructureException("Error finding seat availability", e);
        }
    }

    /**
     * Finds seat availability by flight ID and seat class.
     */
    @Cacheable(value = CACHE_NAME, key = "#flightId + '-' + #seatClass")
    public DomainEntitySeatAvailability findByFlightIdAndSeatClass(UUID flightId, String seatClass) {
        try {
            TypedQuery<DomainEntitySeatAvailability> query = entityManager.createQuery(
                    "SELECT sa FROM DomainEntitySeatAvailability sa " +
                    "WHERE sa.flightId = :flightId AND sa.seatClass = :seatClass",
                    DomainEntitySeatAvailability.class);
            query.setParameter("flightId", flightId);
            query.setParameter("seatClass", seatClass);
            return query.getSingleResult();
        } catch (Exception e) {
            throw new InfrastructureException("Error finding seat availability by class", e);
        }
    }

    /**
     * Finds all seat availability records with less than specified available seats.
     */
    public List<DomainEntitySeatAvailability> findLowAvailability(int threshold) {
        try {
            TypedQuery<DomainEntitySeatAvailability> query = entityManager.createQuery(
                    "SELECT sa FROM DomainEntitySeatAvailability sa " +
                    "WHERE sa.availableSeats <= :threshold " +
                    "ORDER BY sa.availableSeats",
                    DomainEntitySeatAvailability.class);
            query.setParameter("threshold", threshold);
            return query.getResultList();
        } catch (Exception e) {
            throw new InfrastructureException("Error finding low availability seats", e);
        }
    }

    /**
     * Updates seat count for a specific flight and class.
     */
    @CacheEvict(value = CACHE_NAME, key = "#flightId")
    public void updateSeatCount(UUID flightId, String seatClass, int count) {
        try {
            entityManager.createQuery(
                    "UPDATE DomainEntitySeatAvailability sa " +
                    "SET sa.availableSeats = :count, sa.lastUpdated = :now " +
                    "WHERE sa.flightId = :flightId AND sa.seatClass = :seatClass")
                    .setParameter("count", count)
                    .setParameter("now", LocalDateTime.now())
                    .setParameter("flightId", flightId)
                    .setParameter("seatClass", seatClass)
                    .executeUpdate();
        } catch (Exception e) {
            throw new InfrastructureException("Error updating seat count", e);
        }
    }
}
