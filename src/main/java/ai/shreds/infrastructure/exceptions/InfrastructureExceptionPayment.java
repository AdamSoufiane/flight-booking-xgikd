package ai.shreds.infrastructure.exceptions;

import java.util.UUID;

/**
 * Infrastructure layer exception for payment processing errors.
 * Provides detailed error information for infrastructure-related failures.
 */
public class InfrastructureExceptionPayment extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String errorCode;
    private final String componentName;

    /**
     * Creates a new infrastructure exception
     * @param message The error message
     */
    public InfrastructureExceptionPayment(String message) {
        this(message, null, null, null);
    }

    /**
     * Creates a new infrastructure exception with a cause
     * @param message The error message
     * @param cause The cause of the error
     */
    public InfrastructureExceptionPayment(String message, Throwable cause) {
        this(message, cause, null, null);
    }

    /**
     * Creates a new infrastructure exception with error details
     * @param message The error message
     * @param errorCode The error code
     * @param componentName The component where the error occurred
     */
    public InfrastructureExceptionPayment(String message, String errorCode, String componentName) {
        super(message);
        this.errorCode = errorCode;
        this.componentName = componentName;
    }

    /**
     * Creates a new infrastructure exception with all details
     * @param message The error message
     * @param cause The cause of the error
     * @param errorCode The error code
     * @param componentName The component where the error occurred
     */
    public InfrastructureExceptionPayment(String message, Throwable cause, String errorCode, String componentName) {
        super(message, cause);
        this.errorCode = errorCode;
        this.componentName = componentName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getComponentName() {
        return componentName;
    }

    /**
     * Creates an exception for database errors
     * @param operation The database operation that failed
     * @param cause The cause of the error
     * @return A new infrastructure exception
     */
    public static InfrastructureExceptionPayment databaseError(String operation, Throwable cause) {
        return new InfrastructureExceptionPayment(
                String.format("Database operation failed: %s", operation),
                cause,
                "DB_ERROR",
                "Database"
        );
    }

    /**
     * Creates an exception for messaging errors
     * @param operation The messaging operation that failed
     * @param cause The cause of the error
     * @return A new infrastructure exception
     */
    public static InfrastructureExceptionPayment messagingError(String operation, Throwable cause) {
        return new InfrastructureExceptionPayment(
                String.format("Messaging operation failed: %s", operation),
                cause,
                "MSG_ERROR",
                "Messaging"
        );
    }

    /**
     * Creates an exception for external service errors
     * @param serviceName The external service name
     * @param operation The operation that failed
     * @param cause The cause of the error
     * @return A new infrastructure exception
     */
    public static InfrastructureExceptionPayment externalServiceError(String serviceName, String operation, Throwable cause) {
        return new InfrastructureExceptionPayment(
                String.format("%s operation failed: %s", serviceName, operation),
                cause,
                "EXT_SERVICE_ERROR",
                serviceName
        );
    }

    /**
     * Creates an exception for configuration errors
     * @param component The component with invalid configuration
     * @param details The configuration error details
     * @return A new infrastructure exception
     */
    public static InfrastructureExceptionPayment configurationError(String component, String details) {
        return new InfrastructureExceptionPayment(
                String.format("Configuration error in %s: %s", component, details),
                "CONFIG_ERROR",
                component
        );
    }

    /**
     * Creates an exception for transaction errors
     * @param transactionId The transaction ID
     * @param operation The operation that failed
     * @param cause The cause of the error
     * @return A new infrastructure exception
     */
    public static InfrastructureExceptionPayment transactionError(UUID transactionId, String operation, Throwable cause) {
        return new InfrastructureExceptionPayment(
                String.format("Transaction operation failed for %s: %s", transactionId, operation),
                cause,
                "TX_ERROR",
                "Transaction"
        );
    }
}
