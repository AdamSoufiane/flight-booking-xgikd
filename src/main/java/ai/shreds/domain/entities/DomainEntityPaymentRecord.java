package ai.shreds.domain.entities;

import ai.shreds.domain.exceptions.DomainExceptionPayment;
import ai.shreds.domain.value_objects.DomainValueMoney;
import ai.shreds.shared.SharedEnumPaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity representing a payment record.
 * Contains the core transaction information and status.
 */
@Entity
@Table(name = "payment_record")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEntityPaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, unique = true)
    private UUID transactionId;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "authorized_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    })
    private DomainValueMoney authorizedAmount;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SharedEnumPaymentStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Creates a payment status entity from this record
     * @param statusValue The new status value
     * @return A new payment status entity
     */
    public DomainEntityPaymentStatus toStatusEntity(SharedEnumPaymentStatus statusValue) {
        return DomainEntityPaymentStatus.builder()
                .paymentRecordId(this.id)
                .transactionId(this.transactionId)
                .status(statusValue)
                .statusChangedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Updates the status of this payment record
     * @param newStatus The new status to set
     */
    public void updateStatus(SharedEnumPaymentStatus newStatus) {
        if (this.status == newStatus) {
            return;
        }
        validateStatusTransition(newStatus);
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Validates if the status transition is allowed
     * @param newStatus The new status to transition to
     * @throws DomainExceptionPayment if transition is not allowed
     */
    private void validateStatusTransition(SharedEnumPaymentStatus newStatus) {
        if (status != null && status.isTerminal() && newStatus != status) {
            throw new DomainExceptionPayment(
                    String.format("Cannot transition from %s to %s as current status is terminal", status, newStatus));
        }

        if (status == SharedEnumPaymentStatus.FAILED && newStatus != SharedEnumPaymentStatus.FAILED) {
            throw new DomainExceptionPayment("Cannot change status once payment has failed");
        }
    }

    /**
     * Checks if the payment is in a terminal state
     * @return true if the payment is in a terminal state
     */
    public boolean isTerminal() {
        return status != null && status.isTerminal();
    }

    /**
     * Checks if the payment is successful
     * @return true if the payment is completed successfully
     */
    public boolean isSuccessful() {
        return status == SharedEnumPaymentStatus.COMPLETED;
    }

    /**
     * Checks if the payment can be captured
     * @return true if the payment can be captured
     */
    public boolean canCapture() {
        return status == SharedEnumPaymentStatus.AUTHORIZED;
    }

    /**
     * Validates the payment record
     * @throws DomainExceptionPayment if validation fails
     */
    public void validate() {
        if (transactionId == null) {
            throw new DomainExceptionPayment("Transaction ID is required");
        }
        if (requestId == null) {
            throw new DomainExceptionPayment("Request ID is required");
        }
        if (userId == null) {
            throw new DomainExceptionPayment("User ID is required");
        }
        if (authorizedAmount == null) {
            throw new DomainExceptionPayment("Authorized amount is required");
        }
        if (status == null) {
            throw new DomainExceptionPayment("Status is required");
        }
    }
}
