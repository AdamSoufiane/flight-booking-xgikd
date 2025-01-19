package ai.shreds.application.exceptions;

/**
 * Application-level exception for payment processing errors.
 * This exception is thrown when payment processing fails at the application layer.
 */
public class ApplicationPaymentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with a detailed message
     * @param message The detailed message
     */
    public ApplicationPaymentException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with a detailed message and cause
     * @param message The detailed message
     * @param cause The cause of the exception
     */
    public ApplicationPaymentException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with a cause
     * @param cause The cause of the exception
     */
    public ApplicationPaymentException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates an exception for validation errors
     * @param field The field that failed validation
     * @param value The invalid value
     * @return A new ApplicationPaymentException
     */
    public static ApplicationPaymentException validationError(String field, Object value) {
        return new ApplicationPaymentException(
            String.format("Validation failed for field '%s' with value: %s", field, value));
    }

    /**
     * Creates an exception for payment processing errors
     * @param transactionId The transaction ID
     * @param reason The reason for the failure
     * @return A new ApplicationPaymentException
     */
    public static ApplicationPaymentException processingError(String transactionId, String reason) {
        return new ApplicationPaymentException(
            String.format("Payment processing failed for transaction '%s': %s", transactionId, reason));
    }

    /**
     * Creates an exception for payment not found
     * @param transactionId The transaction ID that wasn't found
     * @return A new ApplicationPaymentException
     */
    public static ApplicationPaymentException paymentNotFound(String transactionId) {
        return new ApplicationPaymentException(
            String.format("Payment not found for transaction ID: %s", transactionId));
    }
}
