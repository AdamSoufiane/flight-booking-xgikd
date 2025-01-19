package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.entities.DomainEntityPaymentRecord;
import ai.shreds.domain.entities.DomainEntityPaymentStatus;
import ai.shreds.domain.ports.DomainPortPaymentMessagePublisher;
import ai.shreds.infrastructure.exceptions.InfrastructureExceptionPayment;
import ai.shreds.shared.SharedEnumPaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementation of the payment message publisher port.
 * Handles publishing payment-related events to message broker.
 */
@Slf4j
@Service
public class InfrastructurePaymentMessagePublisherImpl implements DomainPortPaymentMessagePublisher {

    private final InfrastructurePaymentMessagePublisher paymentMessagePublisher;

    @Autowired
    public InfrastructurePaymentMessagePublisherImpl(InfrastructurePaymentMessagePublisher paymentMessagePublisher) {
        this.paymentMessagePublisher = paymentMessagePublisher;
    }

    @Override
    public void publishPaymentStatus(DomainEntityPaymentStatus status) {
        log.debug("Publishing payment status update for record ID: {}", status.getPaymentRecordId());
        try {
            // Now we use status.getTransactionId() instead of paymentRecordId.
            paymentMessagePublisher.publishEvent(
                    status.getTransactionId(),
                    status.getStatus().name(),
                    status.getStatusChangedAt()
            );
            log.info("Successfully published status update for transaction: {}", status.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to publish payment status for record ID: {}", status.getPaymentRecordId(), e);
            throw new InfrastructureExceptionPayment("Failed to publish payment status", e);
        }
    }

    @Override
    public void publishPaymentCreated(DomainEntityPaymentRecord record) {
        log.debug("Publishing payment creation event for transaction: {}", record.getTransactionId());
        try {
            paymentMessagePublisher.publishPaymentCreated(
                    record.getTransactionId(),
                    record.getUserId(),
                    record.getAuthorizedAmount(),
                    record.getCreatedAt()
            );
            log.info("Successfully published payment creation event for transaction: {}", record.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to publish payment creation event for transaction: {}", record.getTransactionId(), e);
            throw new InfrastructureExceptionPayment("Failed to publish payment creation event", e);
        }
    }

    @Override
    public void publishStatusChange(UUID transactionId,
                                   SharedEnumPaymentStatus oldStatus,
                                   SharedEnumPaymentStatus newStatus,
                                   LocalDateTime timestamp) {
        log.debug("Publishing status change event for transaction: {} from {} to {}",
                transactionId, oldStatus, newStatus);
        try {
            paymentMessagePublisher.publishStatusChange(
                    transactionId,
                    oldStatus.name(),
                    newStatus.name(),
                    timestamp
            );
            log.info("Successfully published status change event for transaction: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to publish status change event for transaction: {}", transactionId, e);
            throw new InfrastructureExceptionPayment("Failed to publish status change event", e);
        }
    }

    @Override
    public void publishPaymentError(UUID transactionId, String errorCode, String errorMessage) {
        log.debug("Publishing payment error event for transaction: {} code: {}", transactionId, errorCode);
        try {
            paymentMessagePublisher.publishError(
                    transactionId,
                    errorCode,
                    errorMessage,
                    LocalDateTime.now()
            );
            log.info("Successfully published error event for transaction: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to publish error event for transaction: {}", transactionId, e);
            throw new InfrastructureExceptionPayment("Failed to publish error event", e);
        }
    }

    @Override
    public void publishFraudDetection(UUID transactionId, int fraudScore, String details) {
        log.debug("Publishing fraud detection event for transaction: {} score: {}", transactionId, fraudScore);
        try {
            paymentMessagePublisher.publishFraudAlert(
                    transactionId,
                    fraudScore,
                    details,
                    LocalDateTime.now()
            );
            log.info("Successfully published fraud detection event for transaction: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to publish fraud detection event for transaction: {}", transactionId, e);
            throw new InfrastructureExceptionPayment("Failed to publish fraud detection event", e);
        }
    }

    @Override
    public void publishRefundEvent(UUID transactionId, double refundAmount, String reason) {
        log.debug("Publishing refund event for transaction: {} amount: {}", transactionId, refundAmount);
        try {
            paymentMessagePublisher.publishRefund(
                    transactionId,
                    refundAmount,
                    reason,
                    LocalDateTime.now()
            );
            log.info("Successfully published refund event for transaction: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to publish refund event for transaction: {}", transactionId, e);
            throw new InfrastructureExceptionPayment("Failed to publish refund event", e);
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            return paymentMessagePublisher.checkConnection();
        } catch (Exception e) {
            log.error("Health check failed", e);
            return false;
        }
    }
}
