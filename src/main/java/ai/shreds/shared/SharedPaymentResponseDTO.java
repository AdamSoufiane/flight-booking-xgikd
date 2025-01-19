package ai.shreds.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object representing the response of a payment transaction.
 * Contains the transaction result including any error or fraud alert information.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SharedPaymentResponseDTO {

    @JsonProperty("transaction_id")
    private UUID transactionId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("error")
    private String error;

    @JsonProperty("fraud_alert")
    private String fraudAlert;

    /**
     * Utility method to create an error response
     * @param error The error message
     * @return A SharedPaymentResponseDTO with error information
     */
    public static SharedPaymentResponseDTO error(String error) {
        return SharedPaymentResponseDTO.builder()
                .error(error)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Utility method to create a fraud alert response
     * @param fraudAlert The fraud alert message
     * @return A SharedPaymentResponseDTO with fraud alert information
     */
    public static SharedPaymentResponseDTO fraudAlert(String fraudAlert) {
        return SharedPaymentResponseDTO.builder()
                .fraudAlert(fraudAlert)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Utility method to create a success response
     * @param transactionId The transaction ID
     * @param status The transaction status
     * @return A SharedPaymentResponseDTO with success information
     */
    public static SharedPaymentResponseDTO success(UUID transactionId, String status) {
        return SharedPaymentResponseDTO.builder()
                .transactionId(transactionId)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
