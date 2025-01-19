package ai.shreds.domain.value_objects;

import ai.shreds.domain.exceptions.DomainExceptionPayment;
import jakarta.persistence.Embeddable;
import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/**
 * Immutable value object representing money in the domain.
 * Handles monetary amounts with currency and provides money operations.
 */
@Value
@Embeddable
public class DomainValueMoney {

    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING = RoundingMode.HALF_UP;

    BigDecimal amount;
    String currency;

    /**
     * Creates a new money value object
     * @param amount The monetary amount
     * @param currency The currency code
     */
    public DomainValueMoney(BigDecimal amount, String currency) {
        this.amount = amount != null ? amount.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING) : null;
        this.currency = currency;
        validateMoney();
    }

    /**
     * Validates the money value
     * @throws DomainExceptionPayment if validation fails
     */
    public void validateMoney() {
        if (amount == null) {
            throw new DomainExceptionPayment("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainExceptionPayment("Amount cannot be negative");
        }
        if (currency == null || currency.trim().isEmpty()) {
            throw new DomainExceptionPayment("Currency cannot be null or empty");
        }
        try {
            Currency.getInstance(currency);
        } catch (IllegalArgumentException e) {
            throw new DomainExceptionPayment("Invalid currency code: " + currency);
        }
    }

    /**
     * Adds another money value to this one
     * @param other The money to add
     * @return A new money value object with the sum
     */
    public DomainValueMoney add(DomainValueMoney other) {
        validateSameCurrency(other);
        return new DomainValueMoney(
                this.amount.add(other.amount),
                this.currency
        );
    }

    /**
     * Subtracts another money value from this one
     * @param other The money to subtract
     * @return A new money value object with the difference
     */
    public DomainValueMoney subtract(DomainValueMoney other) {
        validateSameCurrency(other);
        return new DomainValueMoney(
                this.amount.subtract(other.amount),
                this.currency
        );
    }

    /**
     * Multiplies this money value by a factor
     * @param factor The multiplication factor
     * @return A new money value object with the product
     */
    public DomainValueMoney multiply(BigDecimal factor) {
        return new DomainValueMoney(
                this.amount.multiply(factor).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING),
                this.currency
        );
    }

    /**
     * Checks if this money value is greater than another
     * @param other The money to compare with
     * @return true if this amount is greater
     */
    public boolean isGreaterThan(DomainValueMoney other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * Checks if this money value is less than another
     * @param other The money to compare with
     * @return true if this amount is less
     */
    public boolean isLessThan(DomainValueMoney other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    /**
     * Validates that two money values have the same currency
     * @param other The other money value
     * @throws DomainExceptionPayment if currencies don't match
     */
    private void validateSameCurrency(DomainValueMoney other) {
        if (other == null) {
            throw new DomainExceptionPayment("Cannot compare with null money value");
        }
        if (!this.currency.equals(other.currency)) {
            throw new DomainExceptionPayment(
                    String.format("Currency mismatch: %s vs %s", this.currency, other.currency));
        }
    }

    /**
     * Creates a zero amount in the specified currency
     * @param currency The currency code
     * @return A new money value object with zero amount
     */
    public static DomainValueMoney zero(String currency) {
        return new DomainValueMoney(BigDecimal.ZERO, currency);
    }

    @Override
    public String toString() {
        return String.format("%s %s", amount.toString(), currency);
    }
}
