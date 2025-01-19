package ai.shreds.domain.value_objects;

import ai.shreds.domain.entities.DomainEntityPaymentStatus;
import ai.shreds.domain.exceptions.DomainExceptionPayment;
import ai.shreds.shared.SharedEnumPaymentStatus;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Immutable value object representing a payment status.
 * Contains status validation logic and conversion to domain entity.
 */
@Value
public class DomainValuePaymentStatus {

    SharedEnumPaymentStatus status;
    LocalDateTime timestamp;

    /**
     * Creates a new payment status value object
     * @param status The payment status
     */
    public DomainValuePaymentStatus(SharedEnumPaymentStatus status) {
        this.status = status;
        this.timestamp = LocalDateTime.now();
        validateStatusValue();
    }

    /**
     * Creates a new payment status value object with a specific timestamp
     * @param status The payment status
     * @param timestamp The status timestamp
     */
    public DomainValuePaymentStatus(SharedEnumPaymentStatus status, LocalDateTime timestamp) {
        this.status = status;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        validateStatusValue();
    }

    /**
     * Validates the status value
     * @throws DomainExceptionPayment if validation fails
     */
    public void validateStatusValue() {
        if (status == null) {
            throw new DomainExceptionPayment("Payment status cannot be null");
        }
    }

    /**
     * Creates a domain entity from this value object
     * @param paymentRecordId The payment record ID
     * @return A new payment status entity
     */
    public DomainEntityPaymentStatus toDomainEntityPaymentStatus(Long paymentRecordId) {
        if (paymentRecordId == null) {
            throw new DomainExceptionPayment("Payment record ID cannot be null");
        }

        return DomainEntityPaymentStatus.builder()
                .paymentRecordId(paymentRecordId)
                .status(status)
                .statusChangedAt(timestamp)
                .build();
    }

    /**
     * Creates a new status with updated timestamp
     * @return A new payment status value object
     */
    public DomainValuePaymentStatus withCurrentTimestamp() {
        return new DomainValuePaymentStatus(this.status, LocalDateTime.now());
    }

    /**
     * Creates a new status with a different status value
     * @param newStatus The new status value
     * @return A new payment status value object
     */
    public DomainValuePaymentStatus withStatus(SharedEnumPaymentStatus newStatus) {
        return new DomainValuePaymentStatus(newStatus, LocalDateTime.now());
    }

    /**
     * Checks if this status represents a terminal state
     * @return true if this is a terminal status
     */
    public boolean isTerminal() {
        return status != null && status.isTerminal();
    }

    /**
     * Checks if this status represents a success state
     * @return true if this is a success status
     */
    public boolean isSuccess() {
        return status == SharedEnumPaymentStatus.COMPLETED;
    }

    /**
     * Checks if this status represents a failure state
     * @return true if this is a failure status
     */
    public boolean isFailure() {
        return status == SharedEnumPaymentStatus.FAILED;
    }

    /**
     * Checks if this status is more recent than another
     * @param other The other status to compare with
     * @return true if this status is more recent
     */
    public boolean isMoreRecentThan(DomainValuePaymentStatus other) {
        if (other == null) {
            return true;
        }
        return this.timestamp.isAfter(other.timestamp);
    }
}
