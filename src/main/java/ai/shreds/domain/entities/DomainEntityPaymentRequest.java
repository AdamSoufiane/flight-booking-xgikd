package ai.shreds.domain.entities;

import ai.shreds.domain.exceptions.DomainExceptionPayment;
import ai.shreds.domain.value_objects.DomainValueMoney;
import ai.shreds.shared.SharedEnumPaymentStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Domain entity representing a payment request.
 * Contains all necessary information to process a payment transaction.
 */
@Entity
@Table(name = "payment_request")
@Data
@Builder
public class DomainEntityPaymentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "payment_method", length = 30, nullable = false)
    private String paymentMethod;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 3, nullable = false)
    private String currency;

    @Column(name = "request_timestamp", nullable = false)
    private LocalDateTime requestTimestamp;

    @Column(name = "additional_params", columnDefinition = "jsonb")
    private Map<String, Object> additionalParams;

    /**
     * Validates the payment request
     * @throws DomainExceptionPayment if validation fails
     */
    public void validate() {
        if (userId == null) {
            throw new DomainExceptionPayment("User ID is required");
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new DomainExceptionPayment("Payment method is required");
        }
        validateMoney();
        if (requestTimestamp == null) {
            requestTimestamp = LocalDateTime.now();
        }
    }

    /**
     * Validates the money amount and currency
     * @throws DomainExceptionPayment if validation fails
     */
    private void validateMoney() {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainExceptionPayment("Amount must be greater than zero");
        }
        if (currency == null || !currency.matches("^[A-Z]{3}$")) {
            throw new DomainExceptionPayment("Invalid currency code");
        }
    }

    /**
     * Creates a payment record from this request
     * @return A new payment record
     */
    public DomainEntityPaymentRecord toRecordEntity() {
        validate();
        return DomainEntityPaymentRecord.builder()
                .requestId(this.id)
                .userId(this.userId)
                .transactionId(UUID.randomUUID())
                .authorizedAmount(new DomainValueMoney(this.amount, this.currency))
                .status(SharedEnumPaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Adds additional parameters to the request
     * @param key Parameter key
     * @param value Parameter value
     */
    public void addAdditionalParam(String key, Object value) {
        if (additionalParams == null) {
            additionalParams = new HashMap<>();
        }
        additionalParams.put(key, value);
    }

    /**
     * Gets the money value object for this request
     * @return DomainValueMoney representing the amount and currency
     */
    public DomainValueMoney getMoney() {
        return new DomainValueMoney(amount, currency);
    }

    /**
     * Checks if the request is expired
     * @param expirationMinutes Minutes after which request expires
     * @return true if request is expired
     */
    public boolean isExpired(int expirationMinutes) {
        return requestTimestamp.plusMinutes(expirationMinutes).isBefore(LocalDateTime.now());
    }

    /**
     * Creates a copy of this request with a new amount
     * @param newAmount The new amount
     * @return A new payment request
     */
    public DomainEntityPaymentRequest withAmount(BigDecimal newAmount) {
        return DomainEntityPaymentRequest.builder()
                .userId(this.userId)
                .paymentMethod(this.paymentMethod)
                .amount(newAmount)
                .currency(this.currency)
                .requestTimestamp(LocalDateTime.now())
                .additionalParams(new HashMap<>(this.additionalParams))
                .build();
    }
}
