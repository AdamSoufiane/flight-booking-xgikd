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
 * Data Transfer Object representing the current status of a payment transaction.
 * Used for status query responses and status updates.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SharedPaymentStatusResponseDTO {

    @JsonProperty("transaction_id")
    private UUID transactionId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    /**
     * Utility method to create a status response
     * @param transactionId The transaction ID
     * @param userId The user ID
     * @param status The current status
     * @return A SharedPaymentStatusResponseDTO with the current status information
     */
    public static SharedPaymentStatusResponseDTO of(UUID transactionId, Long userId, String status) {
        return SharedPaymentStatusResponseDTO.builder()
                .transactionId(transactionId)
                .userId(userId)
                .status(status)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
