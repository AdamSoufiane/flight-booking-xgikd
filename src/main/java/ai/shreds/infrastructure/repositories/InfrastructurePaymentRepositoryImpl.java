package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.entities.DomainEntityPaymentRequest;
import ai.shreds.domain.entities.DomainEntityPaymentRecord;
import ai.shreds.domain.entities.DomainEntityPaymentStatus;
import ai.shreds.domain.exceptions.DomainExceptionPayment;
import ai.shreds.domain.ports.DomainPortPaymentRepository;
import ai.shreds.shared.SharedEnumPaymentStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA implementation of the payment repository port.
 * Handles all database operations for payment-related entities.
 */
@Slf4j
@Repository
@Transactional
public class InfrastructurePaymentRepositoryImpl implements DomainPortPaymentRepository {

    private static final String PAYMENT_CACHE = "payments";
    private static final String STATUS_CACHE = "payment-statuses";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @CacheEvict(value = PAYMENT_CACHE, allEntries = true)
    public DomainEntityPaymentRequest savePaymentRequest(DomainEntityPaymentRequest request) {
        log.debug("Saving payment request for user: {}", request.getUserId());
        try {
            if (request.getId() == null) {
                entityManager.persist(request);
            } else {
                request = entityManager.merge(request);
            }
            log.info("Saved payment request with ID: {}", request.getId());
            return request;
        } catch (Exception e) {
            log.error("Failed to save payment request", e);
            throw new DomainExceptionPayment("Failed to save payment request", e);
        }
    }

    @Override
    @CacheEvict(value = PAYMENT_CACHE, allEntries = true)
    public DomainEntityPaymentRecord savePaymentRecord(DomainEntityPaymentRecord record) {
        log.debug("Saving payment record with transaction ID: {}", record.getTransactionId());
        try {
            if (record.getId() == null) {
                entityManager.persist(record);
            } else {
                record = entityManager.merge(record);
            }
            log.info("Saved payment record with ID: {}", record.getId());
            return record;
        } catch (Exception e) {
            log.error("Failed to save payment record", e);
            throw new DomainExceptionPayment("Failed to save payment record", e);
        }
    }

    @Override
    @CacheEvict(value = {PAYMENT_CACHE, STATUS_CACHE}, allEntries = true)
    public DomainEntityPaymentStatus savePaymentStatus(DomainEntityPaymentStatus status) {
        log.debug("Saving payment status for record ID: {}", status.getPaymentRecordId());
        try {
            if (status.getId() == null) {
                entityManager.persist(status);
            } else {
                status = entityManager.merge(status);
            }
            log.info("Saved payment status with ID: {}", status.getId());
            return status;
        } catch (Exception e) {
            log.error("Failed to save payment status", e);
            throw new DomainExceptionPayment("Failed to save payment status", e);
        }
    }

    @Override
    @Cacheable(value = PAYMENT_CACHE, key = "#transactionId")
    @Transactional(readOnly = true)
    public Optional<DomainEntityPaymentRecord> findPaymentRecordByTransactionId(UUID transactionId) {
        log.debug("Finding payment record for transaction ID: {}", transactionId);
        try {
            String jpql = "SELECT r FROM DomainEntityPaymentRecord r WHERE r.transactionId = :transactionId";
            TypedQuery<DomainEntityPaymentRecord> query = entityManager.createQuery(jpql, DomainEntityPaymentRecord.class)
                    .setParameter("transactionId", transactionId);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            log.warn("No payment record found for transaction ID: {}", transactionId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error finding payment record", e);
            throw new DomainExceptionPayment("Error finding payment record", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainEntityPaymentRecord> findPaymentRecordsByUserId(Long userId) {
        log.debug("Finding payment records for user ID: {}", userId);
        try {
            String jpql = "SELECT r FROM DomainEntityPaymentRecord r WHERE r.userId = :userId ORDER BY r.createdAt DESC";
            return entityManager.createQuery(jpql, DomainEntityPaymentRecord.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } catch (Exception e) {
            log.error("Error finding payment records for user", e);
            throw new DomainExceptionPayment("Error finding payment records", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainEntityPaymentRecord> findPaymentRecordsByStatus(SharedEnumPaymentStatus status) {
        log.debug("Finding payment records with status: {}", status);
        try {
            String jpql = "SELECT r FROM DomainEntityPaymentRecord r WHERE r.status = :status ORDER BY r.updatedAt DESC";
            return entityManager.createQuery(jpql, DomainEntityPaymentRecord.class)
                    .setParameter("status", status)
                    .getResultList();
        } catch (Exception e) {
            log.error("Error finding payment records by status", e);
            throw new DomainExceptionPayment("Error finding payment records", e);
        }
    }

    @Override
    @Cacheable(value = STATUS_CACHE, key = "#transactionId")
    @Transactional(readOnly = true)
    public Optional<DomainEntityPaymentStatus> findLatestPaymentStatus(UUID transactionId) {
        log.debug("Finding latest payment status for transaction ID: {}", transactionId);
        try {
            String jpql = "SELECT s FROM DomainEntityPaymentStatus s " +
                    "WHERE s.paymentRecordId = (SELECT r.id FROM DomainEntityPaymentRecord r WHERE r.transactionId = :transactionId) " +
                    "ORDER BY s.statusChangedAt DESC";
            List<DomainEntityPaymentStatus> results = entityManager.createQuery(jpql, DomainEntityPaymentStatus.class)
                    .setParameter("transactionId", transactionId)
                    .setMaxResults(1)
                    .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            log.error("Error finding latest payment status", e);
            throw new DomainExceptionPayment("Error finding latest payment status", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DomainEntityPaymentStatus> findPaymentStatusHistory(Long paymentRecordId) {
        log.debug("Finding payment status history for record ID: {}", paymentRecordId);
        try {
            String jpql = "SELECT s FROM DomainEntityPaymentStatus s " +
                    "WHERE s.paymentRecordId = :paymentRecordId ORDER BY s.statusChangedAt DESC";
            return entityManager.createQuery(jpql, DomainEntityPaymentStatus.class)
                    .setParameter("paymentRecordId", paymentRecordId)
                    .getResultList();
        } catch (Exception e) {
            log.error("Error finding payment status history", e);
            throw new DomainExceptionPayment("Error finding payment status history", e);
        }
    }

    @Override
    @CacheEvict(value = {PAYMENT_CACHE, STATUS_CACHE}, allEntries = true)
    public DomainEntityPaymentRecord updatePaymentRecordStatus(DomainEntityPaymentRecord record, SharedEnumPaymentStatus newStatus) {
        log.debug("Updating payment record {} to status {}", record.getTransactionId(), newStatus);
        record.setStatus(newStatus);
        return savePaymentRecord(record);
    }
}
