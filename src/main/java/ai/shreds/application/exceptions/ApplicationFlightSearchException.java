package ai.shreds.application.exceptions;

import lombok.Getter;

/**
 * Base exception for flight search related errors in the application layer.
 * Provides error codes and various constructors for different error scenarios.
 */
@Getter
public class ApplicationFlightSearchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String errorCode;
    private final transient Object[] parameters;

    /**
     * Creates a new exception with message and error code.
     *
     * @param message the error message
     * @param errorCode the error code for client handling
     */
    public ApplicationFlightSearchException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.parameters = new Object[0];
    }

    /**
     * Creates a new exception with message, error code, and cause.
     *
     * @param message the error message
     * @param errorCode the error code for client handling
     * @param cause the underlying cause
     */
    public ApplicationFlightSearchException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.parameters = new Object[0];
    }

    /**
     * Creates a new exception with message and cause.
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public ApplicationFlightSearchException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "FLIGHT_SEARCH_ERROR";
        this.parameters = new Object[0];
    }

    /**
     * Creates a new exception with message, error code, and parameters for message formatting.
     *
     * @param message the error message
     * @param errorCode the error code for client handling
     * @param parameters parameters for message formatting
     */
    public ApplicationFlightSearchException(String message, String errorCode, Object... parameters) {
        super(String.format(message, parameters));
        this.errorCode = errorCode;
        this.parameters = parameters;
    }

    /**
     * Gets a formatted message using the parameters if available.
     *
     * @return the formatted message
     */
    @Override
    public String getMessage() {
        if (parameters != null && parameters.length > 0) {
            return String.format(super.getMessage(), parameters);
        }
        return super.getMessage();
    }
}
