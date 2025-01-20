package ai.shreds.infrastructure.exceptions;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Exception class for infrastructure layer errors.
 * Handles database, external service, and other infrastructure-related errors.
 */
@Getter
public class InfrastructureException extends RuntimeException {

    private final String errorCode;
    private final String details;
    private final LocalDateTime timestamp;
    private final List<String> errors;
    private final String component;
    private final ErrorType errorType;

    /**
     * Types of infrastructure errors.
     */
    public enum ErrorType {
        DATABASE,
        EXTERNAL_SERVICE,
        CACHE,
        NETWORK,
        CONFIGURATION,
        SECURITY,
        UNKNOWN
    }

    /**
     * Creates a new infrastructure exception with a message.
     *
     * @param message Error message
     */
    public InfrastructureException(String message) {
        this(message, null, ErrorType.UNKNOWN);
    }

    /**
     * Creates a new infrastructure exception with a message and cause.
     *
     * @param message Error message
     * @param cause Original cause
     */
    public InfrastructureException(String message, Throwable cause) {
        this(message, cause, determineErrorType(cause));
    }

    /**
     * Creates a new infrastructure exception with full details.
     *
     * @param message Error message
     * @param cause Original cause
     * @param errorType Type of error
     */
    public InfrastructureException(String message, Throwable cause, ErrorType errorType) {
        super(message, cause);
        this.errorCode = generateErrorCode(errorType);
        this.details = cause != null ? cause.getMessage() : null;
        this.timestamp = LocalDateTime.now();
        this.errors = new ArrayList<>();
        this.component = determineComponent();
        this.errorType = errorType;
        initializeErrors(cause);
    }

    /**
     * Creates a database error exception.
     *
     * @param message Error message
     * @param cause Original cause
     * @return Infrastructure exception for database error
     */
    public static InfrastructureException databaseError(String message, Throwable cause) {
        return new InfrastructureException(
                "Database error: " + message,
                cause,
                ErrorType.DATABASE
        );
    }

    /**
     * Creates an external service error exception.
     *
     * @param service Service name
     * @param message Error message
     * @param cause Original cause
     * @return Infrastructure exception for external service error
     */
    public static InfrastructureException externalServiceError(
            String service, String message, Throwable cause) {
        return new InfrastructureException(
                String.format("External service '%s' error: %s", service, message),
                cause,
                ErrorType.EXTERNAL_SERVICE
        );
    }

    /**
     * Creates a cache error exception.
     *
     * @param operation Operation that failed
     * @param message Error message
     * @return Infrastructure exception for cache error
     */
    public static InfrastructureException cacheError(String operation, String message) {
        return new InfrastructureException(
                String.format("Cache operation '%s' failed: %s", operation, message),
                null,
                ErrorType.CACHE
        );
    }

    /**
     * Creates a network error exception.
     *
     * @param endpoint Endpoint that failed
     * @param message Error message
     * @param cause Original cause
     * @return Infrastructure exception for network error
     */
    public static InfrastructureException networkError(
            String endpoint, String message, Throwable cause) {
        return new InfrastructureException(
                String.format("Network error for endpoint '%s': %s", endpoint, message),
                cause,
                ErrorType.NETWORK
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
                .append("Infrastructure Error [")
                .append(errorCode)
                .append("] in ")
                .append(component)
                .append(" (")
                .append(errorType)
                .append("): ")
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

    private static ErrorType determineErrorType(Throwable cause) {
        if (cause == null) return ErrorType.UNKNOWN;
        String className = cause.getClass().getName().toLowerCase();
        
        if (className.contains("sql") || className.contains("database")) {
            return ErrorType.DATABASE;
        } else if (className.contains("http") || className.contains("rest")) {
            return ErrorType.EXTERNAL_SERVICE;
        } else if (className.contains("redis") || className.contains("cache")) {
            return ErrorType.CACHE;
        } else if (className.contains("socket") || className.contains("connect")) {
            return ErrorType.NETWORK;
        }
        
        return ErrorType.UNKNOWN;
    }

    private String generateErrorCode(ErrorType type) {
        return "INFRA_" + type.name();
    }

    private void initializeErrors(Throwable cause) {
        if (cause != null) {
            errors.add(cause.getMessage());
            if (cause.getCause() != null) {
                errors.add("Caused by: " + cause.getCause().getMessage());
            }
        }
    }

    private String determineComponent() {
        StackTraceElement[] stackTrace = getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            if (className.contains(".infrastructure.") && !className.contains(".exceptions.")) {
                return className.substring(className.lastIndexOf(".") + 1);
            }
        }
        return "Unknown";
    }
}
