package ai.shreds.domain.exceptions;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Exception class for domain layer errors.
 */
@Getter
public class DomainException extends RuntimeException {

    private final String errorCode;
    private final String details;
    private final LocalDateTime timestamp;
    private final List<String> validationErrors;
    private final String domain;

    /**
     * Creates a new domain exception with a message.
     *
     * @param message Error message
     */
    public DomainException(String message) {
        this(message, null, null);
    }

    /**
     * Creates a new domain exception with a message and cause.
     *
     * @param message Error message
     * @param cause Original cause
     */
    public DomainException(String message, Throwable cause) {
        this(message, cause, null);
    }

    /**
     * Creates a new domain exception with a message and error code.
     *
     * @param message Error message
     * @param errorCode Error code
     */
    public DomainException(String message, String errorCode) {
        this(message, null, errorCode);
    }

    /**
     * Creates a new domain exception with full details.
     *
     * @param message Error message
     * @param cause Original cause
     * @param errorCode Error code
     */
    public DomainException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : "DOMAIN_ERROR";
        this.details = cause != null ? cause.getMessage() : null;
        this.timestamp = LocalDateTime.now();
        this.validationErrors = new ArrayList<>();
        this.domain = determineDomain();
    }

    /**
     * Creates a validation exception with multiple errors.
     *
     * @param message Error message
     * @param validationErrors List of validation errors
     * @return Domain exception with validation errors
     */
    public static DomainException validationError(String message, List<String> validationErrors) {
        DomainException ex = new DomainException(message, "VALIDATION_ERROR");
        ex.validationErrors.addAll(validationErrors);
        return ex;
    }

    /**
     * Creates a not found exception.
     *
     * @param entityType Type of entity not found
     * @param identifier Entity identifier
     * @return Domain exception for not found entity
     */
    public static DomainException notFound(String entityType, String identifier) {
        return new DomainException(
                String.format("%s not found with identifier: %s", entityType, identifier),
                "NOT_FOUND_ERROR"
        );
    }

    /**
     * Creates an invalid state exception.
     *
     * @param message Error message
     * @return Domain exception for invalid state
     */
    public static DomainException invalidState(String message) {
        return new DomainException(message, "INVALID_STATE_ERROR");
    }

    /**
     * Creates a business rule violation exception.
     *
     * @param rule Rule that was violated
     * @param details Details about the violation
     * @return Domain exception for business rule violation
     */
    public static DomainException businessRuleViolation(String rule, String details) {
        return new DomainException(
                String.format("Business rule violated: %s - %s", rule, details),
                "BUSINESS_RULE_ERROR"
        );
    }

    /**
     * Adds a validation error to the exception.
     *
     * @param error Validation error message
     */
    public void addValidationError(String error) {
        this.validationErrors.add(error);
    }

    /**
     * Gets a formatted error message including all details.
     *
     * @return Formatted error message
     */
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder()
                .append("Domain Error [")
                .append(errorCode)
                .append("] in ")
                .append(domain)
                .append(": ")
                .append(getMessage());

        if (!validationErrors.isEmpty()) {
            sb.append("\nValidation Errors:\n")
              .append(String.join("\n", validationErrors));
        }

        if (details != null) {
            sb.append("\nDetails: ")
              .append(details);
        }

        return sb.toString();
    }

    private String determineDomain() {
        StackTraceElement[] stackTrace = getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.contains(".domain.") && !className.contains(".exceptions.")) {
                return className.substring(className.lastIndexOf(".") + 1);
            }
        }
        return "Unknown";
    }
}
