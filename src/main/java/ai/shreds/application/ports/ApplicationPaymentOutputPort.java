package ai.shreds.application.ports;

import ai.shreds.shared.SharedPaymentEventDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Secondary port for publishing payment events from the application layer.
 * Defines the interface for outgoing payment status notifications.
 */
public interface ApplicationPaymentOutputPort {

    /**
     * Publishes a payment event to the messaging system
     *
     * @param event The payment event to publish
     * @throws ai.shreds.application.exceptions.ApplicationPaymentException if event publishing fails
     */
    void publishPaymentEvent(@NotNull @Valid SharedPaymentEventDTO event);
}
