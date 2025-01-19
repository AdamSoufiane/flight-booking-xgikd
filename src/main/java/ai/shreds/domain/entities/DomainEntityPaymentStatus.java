package ai.shreds.domain.entities;

import ai.shreds.domain.exceptions.DomainExceptionPayment;
import ai.shreds.shared.SharedEnumPaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity representing a payment status change.
 * Tracks the history of status changes for a payment record.
 */
@Entity
@Table(name = "payment_status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEntityPaymentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_record_id", nullable = false)
    private Long paymentRecordId;

    /**
     * Added to store the transactionId from DomainEntityPaymentRecord to avoid mismatch.
     */
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SharedEnumPaymentStatus status;

    @Column(name = "status_changed_at", nullable = false)
    private LocalDateTime statusChangedAt;

    /**
     * Validates the payment status entity
     * @throws DomainExceptionPayment if validation fails
     */
    public void validate() {
        if (paymentRecordId == null) {
            throw new DomainExceptionPayment("Payment record ID is required");
        }
        if (transactionId == null) {
            throw new DomainExceptionPayment("Transaction ID is required");
        }
        if (status == null) {
            throw new DomainExceptionPayment("Status is required");
        }
        if (statusChangedAt == null) {
            statusChangedAt = LocalDateTime.now();
        }
    }

    /**
     * Creates a new status entity with the same payment record ID but different status
     * @param newStatus The new status
     * @return A new payment status entity
     */
    public DomainEntityPaymentStatus withNewStatus(SharedEnumPaymentStatus newStatus) {
        return DomainEntityPaymentStatus.builder()
                .paymentRecordId(this.paymentRecordId)
                .transactionId(this.transactionId)
                .status(newStatus)
                .statusChangedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Checks if this status change is more recent than another
     * @param other The other status change to compare with
     * @return true if this status change is more recent
     */
    public boolean isMoreRecentThan(DomainEntityPaymentStatus other) {
        if (other == null) {
            return true;
        }
        return this.statusChangedAt.isAfter(other.statusChangedAt);
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
     * Creates a copy of this status with current timestamp
     * @return A new payment status entity
     */
    public DomainEntityPaymentStatus withCurrentTimestamp() {
        return DomainEntityPaymentStatus.builder()
                .paymentRecordId(this.paymentRecordId)
                .transactionId(this.transactionId)
                .status(this.status)
                .statusChangedAt(LocalDateTime.now())
                .build();
    }
}
