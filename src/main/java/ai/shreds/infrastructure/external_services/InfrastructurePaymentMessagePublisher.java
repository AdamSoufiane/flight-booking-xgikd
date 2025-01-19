package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.value_objects.DomainValueMoney;
import ai.shreds.infrastructure.exceptions.InfrastructureExceptionPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Publisher for payment-related events to message broker.
 * Currently implements mock behavior for testing purposes.
 */
@Slf4j
@Component
public class InfrastructurePaymentMessagePublisher {

    @Value("${payment.events.exchange}")
    private String exchange;

    @Value("${payment.events.routing-key-prefix}")
    private String routingKeyPrefix;

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public InfrastructurePaymentMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes a generic payment event
     */
    public void publishEvent(UUID transactionId, String newStatus, LocalDateTime timestamp) {
        log.debug("Publishing payment event for transaction: {}", transactionId);
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("transactionId", transactionId);
            event.put("status", newStatus);
            event.put("timestamp", timestamp);

            publishToQueue("status", event);
            log.info("Successfully published payment event for transaction: {}", transactionId);
        } catch (Exception e) {
            handlePublishError(e);
        }
    }

    /**
     * Publishes a payment creation event
     */
    public void publishPaymentCreated(UUID transactionId, Long userId, DomainValueMoney amount, LocalDateTime timestamp) {
        log.debug("Publishing payment creation event for transaction: {}", transactionId);
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("transactionId", transactionId);
            event.put("userId", userId);
            event.put("amount", amount.getAmount());
            event.put("currency", amount.getCurrency());
            event.put("timestamp", timestamp);

            publishToQueue("created", event);
            log.info("Successfully published payment creation event for transaction: {}", transactionId);
        } catch (Exception e) {
            handlePublishError(e);
        }
    }

    /**
     * Publishes a status change event
     */
    public void publishStatusChange(UUID transactionId, String oldStatus, String newStatus, LocalDateTime timestamp) {
        log.debug("Publishing status change event for transaction: {}", transactionId);
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("transactionId", transactionId);
            event.put("oldStatus", oldStatus);
            event.put("newStatus", newStatus);
            event.put("timestamp", timestamp);

            publishToQueue("status-change", event);
            log.info("Successfully published status change event for transaction: {}", transactionId);
        } catch (Exception e) {
            handlePublishError(e);
        }
    }

    /**
     * Publishes an error event
     */
    public void publishError(UUID transactionId, String errorCode, String errorMessage, LocalDateTime timestamp) {
        log.debug("Publishing error event for transaction: {}", transactionId);
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("transactionId", transactionId);
            event.put("errorCode", errorCode);
            event.put("errorMessage", errorMessage);
            event.put("timestamp", timestamp);

            publishToQueue("error", event);
            log.info("Successfully published error event for transaction: {}", transactionId);
        } catch (Exception e) {
            handlePublishError(e);
        }
    }

    /**
     * Publishes a fraud alert event
     */
    public void publishFraudAlert(UUID transactionId, int fraudScore, String details, LocalDateTime timestamp) {
        log.debug("Publishing fraud alert event for transaction: {}", transactionId);
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("transactionId", transactionId);
            event.put("fraudScore", fraudScore);
            event.put("details", details);
            event.put("timestamp", timestamp);

            publishToQueue("fraud-alert", event);
            log.info("Successfully published fraud alert event for transaction: {}", transactionId);
        } catch (Exception e) {
            handlePublishError(e);
        }
    }

    /**
     * Publishes a refund event
     */
    public void publishRefund(UUID transactionId, double refundAmount, String reason, LocalDateTime timestamp) {
        log.debug("Publishing refund event for transaction: {}", transactionId);
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("transactionId", transactionId);
            event.put("refundAmount", refundAmount);
            event.put("reason", reason);
            event.put("timestamp", timestamp);

            publishToQueue("refund", event);
            log.info("Successfully published refund event for transaction: {}", transactionId);
        } catch (Exception e) {
            handlePublishError(e);
        }
    }

    /**
     * Checks if the connection to the message broker is healthy
     */
    public boolean checkConnection() {
        try {
            rabbitTemplate.execute(channel -> {
                channel.isOpen();
                return null;
            });
            return true;
        } catch (Exception e) {
            log.error("Connection check failed", e);
            return false;
        }
    }

    private void publishToQueue(String eventType, Map<String, Object> event) {
        String routingKey = routingKeyPrefix + "." + eventType;
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

    private void handlePublishError(Exception error) {
        log.error("Failed to publish message", error);
        throw new InfrastructureExceptionPayment("Failed to publish message: " + error.getMessage(), error);
    }
}
