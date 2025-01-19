package ai.shreds.shared;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Represents the payment request parameters received from clients.
 * Contains all necessary information to process a payment transaction.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SharedPaymentRequestParams {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Payment method is required")
    @Size(min = 1, max = 30, message = "Payment method must be between 1 and 30 characters")
    private String paymentMethod;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO currency code")
    private String currency;

    private Map<String, Object> additionalParams;
}
