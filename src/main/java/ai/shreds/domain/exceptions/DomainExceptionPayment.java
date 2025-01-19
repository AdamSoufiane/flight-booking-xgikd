package ai.shreds.domain.exceptions;

import java.util.UUID;

/**
 * Domain exception for payment processing errors.
 * Provides detailed error information for payment-related failures.
 */
public class DomainExceptionPayment extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String errorCode;
    private final String transactionId;

    /**
     * Creates a new payment exception
     * @param message The error message
     */
    public DomainExceptionPayment(String message) {
        this(message, null, null);
    }

    /**
     * Creates a new payment exception with a cause
     * @param message The error message
     * @param cause The cause of the error
     */
    public DomainExceptionPayment(String message, Throwable cause) {
        this(message, cause, null, null);
    }

    /**
     * Creates a new payment exception with an error code
     * @param message The error message
     * @param errorCode The error code
     */
    public DomainExceptionPayment(String message, String errorCode) {
        this(message, null, errorCode, null);
    }

    /**
     * Creates a new payment exception with transaction details
     * @param message The error message
     * @param transactionId The transaction ID
     * @param errorCode The error code
     */
    public DomainExceptionPayment(String message, String errorCode, String transactionId) {
        super(message);
        this.errorCode = errorCode;
        this.transactionId = transactionId;
    }

    /**
     * Creates a new payment exception with all details
     * @param message The error message
     * @param cause The cause of the error
     * @param errorCode The error code
     * @param transactionId The transaction ID
     */
    public DomainExceptionPayment(String message, Throwable cause, String errorCode, String transactionId) {
        super(message, cause);
        this.errorCode = errorCode;
        this.transactionId = transactionId;
    }

    /**
     * Gets the error code
     * @return The error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the transaction ID
     * @return The transaction ID
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Creates an exception for invalid amount
     * @param amount The invalid amount
     * @return A new payment exception
     */
    public static DomainExceptionPayment invalidAmount(String amount) {
        return new DomainExceptionPayment(
                String.format("Invalid payment amount: %s", amount),
                "INVALID_AMOUNT");
    }

    /**
     * Creates an exception for payment not found
     * @param transactionId The transaction ID
     * @return A new payment exception
     */
    public static DomainExceptionPayment paymentNotFound(UUID transactionId) {
        return new DomainExceptionPayment(
                String.format("Payment not found: %s", transactionId),
                "PAYMENT_NOT_FOUND",
                transactionId.toString());
    }

    /**
     * Creates an exception for invalid status transition
     * @param currentStatus The current status
     * @param newStatus The new status
     * @return A new payment exception
     */
    public static DomainExceptionPayment invalidStatusTransition(String currentStatus, String newStatus) {
        return new DomainExceptionPayment(
                String.format("Invalid status transition from %s to %s", currentStatus, newStatus),
                "INVALID_STATUS_TRANSITION");
    }

    /**
     * Creates an exception for authorization failure
     * @param transactionId The transaction ID
     * @param reason The reason for failure
     * @return A new payment exception
     */
    public static DomainExceptionPayment authorizationFailed(UUID transactionId, String reason) {
        return new DomainExceptionPayment(
                String.format("Payment authorization failed: %s", reason),
                "AUTHORIZATION_FAILED",
                transactionId.toString());
    }
}
