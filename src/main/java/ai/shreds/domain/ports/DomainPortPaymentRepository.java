package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityPaymentRequest;
import ai.shreds.domain.entities.DomainEntityPaymentRecord;
import ai.shreds.domain.entities.DomainEntityPaymentStatus;
import ai.shreds.shared.SharedEnumPaymentStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for payment-related persistence operations.
 * Defines the contract for data access operations in the domain layer.
 */
public interface DomainPortPaymentRepository {

    /**
     * Saves a new payment request
     * @param request The payment request to save
     * @return The saved payment request with generated ID
     */
    DomainEntityPaymentRequest savePaymentRequest(DomainEntityPaymentRequest request);

    /**
     * Saves a payment record
     * @param record The payment record to save
     * @return The saved payment record
     */
    DomainEntityPaymentRecord savePaymentRecord(DomainEntityPaymentRecord record);

    /**
     * Saves a payment status
     * @param status The payment status to save
     * @return The saved payment status
     */
    DomainEntityPaymentStatus savePaymentStatus(DomainEntityPaymentStatus status);

    /**
     * Finds a payment record by its transaction ID
     * @param transactionId The transaction ID to search for
     * @return Optional containing the payment record if found
     */
    Optional<DomainEntityPaymentRecord> findPaymentRecordByTransactionId(UUID transactionId);

    /**
     * Finds all payment records for a specific user
     * @param userId The user ID to search for
     * @return List of payment records
     */
    List<DomainEntityPaymentRecord> findPaymentRecordsByUserId(Long userId);

    /**
     * Finds all payment records with a specific status
     * @param status The payment status to search for
     * @return List of payment records
     */
    List<DomainEntityPaymentRecord> findPaymentRecordsByStatus(SharedEnumPaymentStatus status);

    /**
     * Finds the latest payment status for a transaction
     * @param transactionId The transaction ID
     * @return Optional containing the latest payment status if found
     */
    Optional<DomainEntityPaymentStatus> findLatestPaymentStatus(UUID transactionId);

    /**
     * Finds all status history for a payment record
     * @param paymentRecordId The payment record ID
     * @return List of payment statuses ordered by timestamp
     */
    List<DomainEntityPaymentStatus> findPaymentStatusHistory(Long paymentRecordId);

    /**
     * Updates the status of a payment record
     * @param record The payment record to update
     * @param newStatus The new status
     * @return The updated payment record
     */
    DomainEntityPaymentRecord updatePaymentRecordStatus(DomainEntityPaymentRecord record, SharedEnumPaymentStatus newStatus);
}
