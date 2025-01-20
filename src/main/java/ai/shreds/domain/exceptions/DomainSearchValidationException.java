package ai.shreds.domain.exceptions;

import lombok.Getter;

/**
 * Exception thrown when flight search validation fails.
 * Used in domain layer to indicate invalid search parameters.
 */
@Getter
public class DomainSearchValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final String validationField;
    private final String validationCode;

    /**
     * Creates a new validation exception with a message.
     *
     * @param message error message
     */
    public DomainSearchValidationException(String message) {
        super(message);
        this.validationField = null;
        this.validationCode = "VALIDATION_ERROR";
    }

    /**
     * Creates a new validation exception with message and field.
     *
     * @param message error message
     * @param validationField the field that failed validation
     */
    public DomainSearchValidationException(String message, String validationField) {
        super(message);
        this.validationField = validationField;
        this.validationCode = "VALIDATION_ERROR";
    }

    /**
     * Creates a new validation exception with message, field, and code.
     *
     * @param message error message
     * @param validationField the field that failed validation
     * @param validationCode specific validation error code
     */
    public DomainSearchValidationException(String message, String validationField, String validationCode) {
        super(message);
        this.validationField = validationField;
        this.validationCode = validationCode;
    }

    /**
     * Creates a new validation exception with message, cause, and field.
     *
     * @param message error message
     * @param cause the cause of this exception
     * @param validationField the field that failed validation
     */
    public DomainSearchValidationException(String message, Throwable cause, String validationField) {
        super(message, cause);
        this.validationField = validationField;
        this.validationCode = "VALIDATION_ERROR";
    }

    /**
     * Gets a detailed message including validation field if available.
     *
     * @return detailed error message
     */
    @Override
    public String getMessage() {
        if (validationField != null && !validationField.isEmpty()) {
            return super.getMessage() + " (Field: " + validationField + ", Code: " + validationCode + ")";
        }
        return super.getMessage();
    }
}
