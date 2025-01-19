package ai.shreds.application.ports;

import ai.shreds.shared.SharedPaymentRequestParams;
import ai.shreds.shared.SharedPaymentResponseDTO;
import ai.shreds.shared.SharedPaymentStatusResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Primary port for payment operations in the application layer.
 * Defines the interface for incoming payment requests and status queries.
 */
public interface ApplicationPaymentInputPort {

    /**
     * Creates a new payment transaction
     *
     * @param params The payment request parameters
     * @return Payment response containing transaction details
     * @throws ai.shreds.application.exceptions.ApplicationPaymentException if payment processing fails
     * @throws ai.shreds.domain.exceptions.DomainExceptionFraudCheck if fraud is detected
     */
    SharedPaymentResponseDTO createPayment(@NotNull @Valid SharedPaymentRequestParams params);

    /**
     * Retrieves the current status of a payment transaction
     *
     * @param transactionId The unique identifier of the transaction
     * @return Current status of the payment transaction
     * @throws ai.shreds.application.exceptions.ApplicationPaymentException if status retrieval fails
     * @throws jakarta.persistence.EntityNotFoundException if transaction is not found
     */
    SharedPaymentStatusResponseDTO getPaymentStatus(@NotNull UUID transactionId);
}
