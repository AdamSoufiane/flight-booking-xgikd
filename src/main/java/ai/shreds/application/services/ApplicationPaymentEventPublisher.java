package ai.shreds.application.services;

import ai.shreds.application.exceptions.ApplicationPaymentException;
import ai.shreds.application.ports.ApplicationPaymentOutputPort;
import ai.shreds.shared.SharedPaymentEventDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Implementation of the payment event publisher.
 * Handles publishing payment events to RabbitMQ message broker.
 */
@Slf4j
@Service
public class ApplicationPaymentEventPublisher implements ApplicationPaymentOutputPort {

    private final AmqpTemplate amqpTemplate;
    private final ObjectMapper objectMapper;

    @Value("${payment.events.exchange}")
    private String exchange;

    @Value("${payment.events.routing-key}")
    private String routingKey;

    public ApplicationPaymentEventPublisher(AmqpTemplate amqpTemplate, ObjectMapper objectMapper) {
        this.amqpTemplate = amqpTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publishPaymentEvent(SharedPaymentEventDTO event) {
        try {
            log.debug("Publishing payment event for transaction: {}", event.getTransactionId());
            
            MessageProperties properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            properties.setHeader("transaction_id", event.getTransactionId().toString());
            properties.setHeader("event_type", "PAYMENT_STATUS_CHANGE");

            byte[] body = objectMapper.writeValueAsBytes(event);
            Message message = new Message(body, properties);

            amqpTemplate.send(exchange, routingKey, message);
            
            log.info("Successfully published payment event for transaction: {}, new status: {}",
                    event.getTransactionId(), event.getNewStatus());

        } catch (Exception e) {
            log.error("Failed to publish payment event for transaction: {}", 
                    event.getTransactionId(), e);
            throw new ApplicationPaymentException(
                    "Failed to publish payment event: " + e.getMessage(), e);
        }
    }
}
