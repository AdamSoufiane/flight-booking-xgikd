package ai.shreds.application.exceptions;

import lombok.Getter;

/**
 * Exception thrown when flight search parameters are invalid.
 * Provides specific error handling for validation failures.
 */
@Getter
public class ApplicationInvalidSearchException extends ApplicationFlightSearchException {

    private static final long serialVersionUID = 1L;
    private final String field;

    /**
     * Creates a new validation exception with message and error code.
     *
     * @param message the error message
     * @param errorCode the error code for client handling
     */
    public ApplicationInvalidSearchException(String message, String errorCode) {
        super(message, errorCode);
        this.field = null;
    }

    /**
     * Creates a new validation exception with message, error code, and field name.
     *
     * @param message the error message
     * @param errorCode the error code for client handling
     * @param field the name of the field that failed validation
     */
    public ApplicationInvalidSearchException(String message, String errorCode, String field) {
        super(message, errorCode);
        this.field = field;
    }

    /**
     * Creates a new validation exception with message, error code, field name, and parameters.
     *
     * @param message the error message
     * @param errorCode the error code for client handling
     * @param field the name of the field that failed validation
     * @param parameters parameters for message formatting
     */
    public ApplicationInvalidSearchException(String message, String errorCode, String field, Object... parameters) {
        super(message, errorCode, parameters);
        this.field = field;
    }

    /**
     * Creates a new validation exception with cause.
     *
     * @param message the error message
     * @param errorCode the error code for client handling
     * @param cause the underlying cause
     */
    public ApplicationInvalidSearchException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
        this.field = null;
    }

    /**
     * Checks if this exception is related to a specific field.
     *
     * @return true if the exception is field-specific
     */
    public boolean hasField() {
        return field != null && !field.isEmpty();
    }

    /**
     * Gets a detailed message including field information if available.
     *
     * @return the detailed error message
     */
    @Override
    public String getMessage() {
        if (hasField()) {
            return String.format("Validation failed for field '%s': %s", field, super.getMessage());
        }
        return super.getMessage();
    }
}
