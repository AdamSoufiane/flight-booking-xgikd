package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityAirlineInfo;
import ai.shreds.domain.ports.DomainPortAirlineInfoRepository;
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
 * Implementation of the airline information repository interface.
 * Handles persistence operations for airline information with caching support.
 */
@Repository
@Transactional
public class InfrastructureAirlineInfoRepositoryImpl implements DomainPortAirlineInfoRepository {

    private static final String CACHE_NAME = "airlineInfo";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public void createAirlineInfo(DomainEntityAirlineInfo entity) {
        try {
            entity.setLastUpdated(LocalDateTime.now());
            entityManager.persist(entity);
        } catch (Exception e) {
            throw new InfrastructureException("Error creating airline info", e);
        }
    }

    @Override
    @CacheEvict(value = CACHE_NAME, key = "#entity.airlineId")
    public void updateAirlineInfo(DomainEntityAirlineInfo entity) {
        try {
            entity.setLastUpdated(LocalDateTime.now());
            entityManager.merge(entity);
        } catch (Exception e) {
            throw new InfrastructureException("Error updating airline info", e);
        }
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "#airlineId")
    public DomainEntityAirlineInfo findAirlineInfoById(String airlineId) {
        try {
            UUID uuid = UUID.fromString(airlineId);
            return findById(uuid);
        } catch (IllegalArgumentException e) {
            throw new InfrastructureException("Invalid airline ID format: " + airlineId, e);
        } catch (Exception e) {
            throw new InfrastructureException("Error finding airline info", e);
        }
    }

    @Override
    @CachePut(value = CACHE_NAME, key = "#airlineInfo.airlineId")
    public DomainEntityAirlineInfo save(DomainEntityAirlineInfo airlineInfo) {
        try {
            airlineInfo.setLastUpdated(LocalDateTime.now());
            if (airlineInfo.getAirlineId() == null) {
                entityManager.persist(airlineInfo);
                return airlineInfo;
            } else {
                return entityManager.merge(airlineInfo);
            }
        } catch (Exception e) {
            throw new InfrastructureException("Error saving airline info", e);
        }
    }

    @Override
    @Cacheable(value = CACHE_NAME, key = "#id")
    public DomainEntityAirlineInfo findById(UUID id) {
        try {
            return entityManager.find(DomainEntityAirlineInfo.class, id);
        } catch (Exception e) {
            throw new InfrastructureException("Error finding airline info by UUID", e);
        }
    }

    /**
     * Finds airlines by country.
     */
    @Cacheable(value = CACHE_NAME, key = "'country-' + #country")
    public List<DomainEntityAirlineInfo> findByCountry(String country) {
        try {
            TypedQuery<DomainEntityAirlineInfo> query = entityManager.createQuery(
                    "SELECT ai FROM DomainEntityAirlineInfo ai " +
                    "WHERE ai.country = :country " +
                    "ORDER BY ai.airlineName",
                    DomainEntityAirlineInfo.class);
            query.setParameter("country", country);
            return query.getResultList();
        } catch (Exception e) {
            throw new InfrastructureException("Error finding airlines by country", e);
        }
    }

    /**
     * Finds airlines by alliance membership.
     */
    @Cacheable(value = CACHE_NAME, key = "'alliance-' + #alliance")
    public List<DomainEntityAirlineInfo> findByAlliance(String alliance) {
        try {
            TypedQuery<DomainEntityAirlineInfo> query = entityManager.createQuery(
                    "SELECT ai FROM DomainEntityAirlineInfo ai " +
                    "WHERE ai.alliance = :alliance " +
                    "ORDER BY ai.airlineName",
                    DomainEntityAirlineInfo.class);
            query.setParameter("alliance", alliance);
            return query.getResultList();
        } catch (Exception e) {
            throw new InfrastructureException("Error finding airlines by alliance", e);
        }
    }

    /**
     * Finds airlines operating in a specific region.
     */
    @Cacheable(value = CACHE_NAME, key = "'region-' + #region")
    public List<DomainEntityAirlineInfo> findByOperatingRegion(String region) {
        try {
            TypedQuery<DomainEntityAirlineInfo> query = entityManager.createQuery(
                    "SELECT DISTINCT ai FROM DomainEntityAirlineInfo ai " +
                    "WHERE :region MEMBER OF ai.operatingRegions " +
                    "ORDER BY ai.airlineName",
                    DomainEntityAirlineInfo.class);
            query.setParameter("region", region);
            return query.getResultList();
        } catch (Exception e) {
            throw new InfrastructureException("Error finding airlines by operating region", e);
        }
    }

    /**
     * Finds active airlines with a minimum fleet size.
     */
    public List<DomainEntityAirlineInfo> findActiveAirlinesWithMinFleetSize(int minFleetSize) {
        try {
            TypedQuery<DomainEntityAirlineInfo> query = entityManager.createQuery(
                    "SELECT ai FROM DomainEntityAirlineInfo ai " +
                    "WHERE ai.isActive = true AND ai.fleetSize >= :minFleetSize " +
                    "ORDER BY ai.fleetSize DESC",
                    DomainEntityAirlineInfo.class);
            query.setParameter("minFleetSize", minFleetSize);
            return query.getResultList();
        } catch (Exception e) {
            throw new InfrastructureException("Error finding airlines by fleet size", e);
        }
    }
}
