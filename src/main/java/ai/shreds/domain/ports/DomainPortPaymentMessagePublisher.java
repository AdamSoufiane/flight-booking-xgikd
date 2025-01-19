package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityPaymentRecord;
import ai.shreds.domain.entities.DomainEntityPaymentStatus;
import ai.shreds.shared.SharedEnumPaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Port for publishing payment-related events.
 * Defines the contract for publishing payment status updates and other payment-related events.
 */
public interface DomainPortPaymentMessagePublisher {

    /**
     * Publishes a payment status update
     * @param status The new payment status
     * @throws ai.shreds.domain.exceptions.DomainExceptionPayment if publishing fails
     */
    void publishPaymentStatus(DomainEntityPaymentStatus status);

    /**
     * Publishes a payment creation event
     * @param record The newly created payment record
     */
    void publishPaymentCreated(DomainEntityPaymentRecord record);

    /**
     * Publishes a payment status change event
     * @param transactionId The transaction ID
     * @param oldStatus The previous status
     * @param newStatus The new status
     * @param timestamp The time of the status change
     */
    void publishStatusChange(UUID transactionId, 
                           SharedEnumPaymentStatus oldStatus,
                           SharedEnumPaymentStatus newStatus,
                           LocalDateTime timestamp);

    /**
     * Publishes a payment error event
     * @param transactionId The transaction ID
     * @param errorCode The error code
     * @param errorMessage The error message
     */
    void publishPaymentError(UUID transactionId, String errorCode, String errorMessage);

    /**
     * Publishes a fraud detection event
     * @param transactionId The transaction ID
     * @param fraudScore The fraud score
     * @param details Additional fraud detection details
     */
    void publishFraudDetection(UUID transactionId, int fraudScore, String details);

    /**
     * Publishes a payment refund event
     * @param transactionId The transaction ID
     * @param refundAmount The refund amount
     * @param reason The reason for refund
     */
    void publishRefundEvent(UUID transactionId, double refundAmount, String reason);

    /**
     * Checks if the publisher is healthy and ready to publish messages
     * @return true if the publisher is healthy
     */
    boolean isHealthy();
}
