package ai.shreds.infrastructure.exceptions;

import lombok.Getter;

/**
 * Exception thrown when database operations fail in the infrastructure layer.
 * Provides error codes and detailed messages for different types of database failures.
 */
@Getter
public class InfrastructureDatabaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final String errorCode;
    private final String operation;

    /**
     * Creates a new database exception with message and error code.
     *
     * @param message error message
     * @param errorCode specific error code for the failure
     */
    public InfrastructureDatabaseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.operation = null;
    }

    /**
     * Creates a new database exception with message, error code, and operation.
     *
     * @param message error message
     * @param errorCode specific error code for the failure
     * @param operation database operation that failed
     */
    public InfrastructureDatabaseException(String message, String errorCode, String operation) {
        super(message);
        this.errorCode = errorCode;
        this.operation = operation;
    }

    /**
     * Creates a new database exception with message, cause, and error code.
     *
     * @param message error message
     * @param cause the underlying cause
     * @param errorCode specific error code for the failure
     */
    public InfrastructureDatabaseException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
        this.operation = null;
    }

    /**
     * Creates a new database exception with all details.
     *
     * @param message error message
     * @param cause the underlying cause
     * @param errorCode specific error code for the failure
     * @param operation database operation that failed
     */
    public InfrastructureDatabaseException(String message, Throwable cause, String errorCode, String operation) {
        super(message, cause);
        this.errorCode = errorCode;
        this.operation = operation;
    }

    /**
     * Gets a detailed message including error code and operation if available.
     *
     * @return detailed error message
     */
    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder(super.getMessage());
        message.append(" (Error Code: ").append(errorCode).append(")");
        if (operation != null && !operation.isEmpty()) {
            message.append(" [Operation: ").append(operation).append("]");
        }
        return message.toString();
    }
}
