package ai.shreds.application.exceptions;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Exception class for application layer errors.
 * Provides structured error handling and can wrap domain exceptions.
 */
@Getter
public class ApplicationException extends RuntimeException {

    private final String errorCode;
    private final String details;
    private final LocalDateTime timestamp;
    private final List<String> errors;
    private final String component;
    private final String operation;

    /**
     * Creates a new application exception with a message.
     *
     * @param message Error message
     */
    public ApplicationException(String message) {
        this(message, null, null, null);
    }

    /**
     * Creates a new application exception with a message and cause.
     *
     * @param message Error message
     * @param cause Original cause
     */
    public ApplicationException(String message, Throwable cause) {
        this(message, cause, null, null);
    }

    /**
     * Creates a new application exception with full details.
     *
     * @param message Error message
     * @param cause Original cause
     * @param component Component where the error occurred
     * @param operation Operation that failed
     */
    public ApplicationException(String message, Throwable cause, String component, String operation) {
        super(message, cause);
        this.errorCode = determineErrorCode(cause);
        this.details = cause != null ? cause.getMessage() : null;
        this.timestamp = LocalDateTime.now();
        this.errors = new ArrayList<>();
        this.component = component != null ? component : determineComponent();
        this.operation = operation != null ? operation : determineOperation();
        initializeErrors(cause);
    }

    /**
     * Creates a validation exception.
     *
     * @param message Error message
     * @param validationErrors List of validation errors
     * @return Application exception with validation errors
     */
    public static ApplicationException validationError(String message, List<String> validationErrors) {
        ApplicationException ex = new ApplicationException(message, null, "Validation", "validate");
        ex.errors.addAll(validationErrors);
        return ex;
    }

    /**
     * Creates a not found exception.
     *
     * @param entityType Type of entity not found
     * @param identifier Entity identifier
     * @return Application exception for not found entity
     */
    public static ApplicationException notFound(String entityType, String identifier) {
        return new ApplicationException(
                String.format("%s not found with identifier: %s", entityType, identifier),
                null,
                entityType,
                "find"
        );
    }

    /**
     * Creates an operation failed exception.
     *
     * @param operation Operation that failed
     * @param reason Reason for failure
     * @return Application exception for failed operation
     */
    public static ApplicationException operationFailed(String operation, String reason) {
        return new ApplicationException(
                String.format("Operation '%s' failed: %s", operation, reason),
                null,
                "OperationHandler",
                operation
        );
    }

    /**
     * Adds an error message to the exception.
     *
     * @param error Error message
     */
    public void addError(String error) {
        this.errors.add(error);
    }

    /**
     * Gets a formatted error message including all details.
     *
     * @return Formatted error message
     */
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder()
                .append("Application Error [")
                .append(errorCode)
                .append("] in ")
                .append(component)
                .append(" during ")
                .append(operation)
                .append(": ")
                .append(getMessage());

        if (!errors.isEmpty()) {
            sb.append("\nErrors:\n")
              .append(String.join("\n", errors));
        }

        if (details != null) {
            sb.append("\nDetails: ")
              .append(details);
        }

        return sb.toString();
    }

    private String determineErrorCode(Throwable cause) {
        if (cause instanceof ai.shreds.domain.exceptions.DomainException) {
            return "APP_DOMAIN_" + ((ai.shreds.domain.exceptions.DomainException) cause).getErrorCode();
        }
        return "APP_ERROR";
    }

    private void initializeErrors(Throwable cause) {
        if (cause instanceof ai.shreds.domain.exceptions.DomainException) {
            ai.shreds.domain.exceptions.DomainException domainEx = 
                    (ai.shreds.domain.exceptions.DomainException) cause;
            errors.addAll(domainEx.getValidationErrors());
        }
    }

    private String determineComponent() {
        StackTraceElement[] stackTrace = getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.contains(".application.") && !className.contains(".exceptions.")) {
                return className.substring(className.lastIndexOf(".") + 1);
            }
        }
        return "Unknown";
    }

    private String determineOperation() {
        StackTraceElement[] stackTrace = getStackTrace();
        if (stackTrace.length > 0) {
            return stackTrace[0].getMethodName();
        }
        return "Unknown";
    }
}
