package ai.shreds.shared;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of possible payment status values.
 * Represents the different states a payment transaction can be in.
 */
public enum SharedEnumPaymentStatus {
    
    /**
     * Initial state when payment is created but not yet processed
     */
    PENDING("PENDING"),

    /**
     * Payment has been authorized by the payment gateway
     */
    AUTHORIZED("AUTHORIZED"),

    /**
     * Payment processing has failed
     */
    FAILED("FAILED"),

    /**
     * Payment has been successfully completed
     */
    COMPLETED("COMPLETED");

    private final String value;

    SharedEnumPaymentStatus(String value) {
        this.value = value;
    }

    /**
     * Gets the string value of the status
     * @return The string representation of the status
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Checks if the status represents a terminal state
     * @return true if the status is COMPLETED or FAILED
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }

    /**
     * Checks if the status represents a success state
     * @return true if the status is COMPLETED
     */
    public boolean isSuccess() {
        return this == COMPLETED;
    }

    /**
     * Checks if the status represents a failure state
     * @return true if the status is FAILED
     */
    public boolean isFailure() {
        return this == FAILED;
    }

    /**
     * Checks if the status represents an in-progress state
     * @return true if the status is PENDING or AUTHORIZED
     */
    public boolean isInProgress() {
        return this == PENDING || this == AUTHORIZED;
    }

    /**
     * Converts a string to the corresponding SharedEnumPaymentStatus
     * @param value The string value to convert
     * @return The corresponding SharedEnumPaymentStatus
     * @throws IllegalArgumentException if the value doesn't match any status
     */
    public static SharedEnumPaymentStatus fromString(String value) {
        for (SharedEnumPaymentStatus status : SharedEnumPaymentStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown payment status: " + value);
    }
}
