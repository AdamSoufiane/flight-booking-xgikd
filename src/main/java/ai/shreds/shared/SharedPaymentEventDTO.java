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
 * Data Transfer Object representing a payment status change event.
 * Used for publishing status updates to the message queue.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SharedPaymentEventDTO {

    @JsonProperty("transaction_id")
    private UUID transactionId;

    @JsonProperty("new_status")
    private String newStatus;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    /**
     * Creates a new payment event with the current timestamp
     * @param transactionId The transaction ID
     * @param newStatus The new payment status
     * @return SharedPaymentEventDTO containing the event information
     */
    public static SharedPaymentEventDTO of(UUID transactionId, String newStatus) {
        return SharedPaymentEventDTO.builder()
                .transactionId(transactionId)
                .newStatus(newStatus)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a new payment event with a specific timestamp
     * @param transactionId The transaction ID
     * @param newStatus The new payment status
     * @param timestamp The event timestamp
     * @return SharedPaymentEventDTO containing the event information
     */
    public static SharedPaymentEventDTO of(UUID transactionId, String newStatus, LocalDateTime timestamp) {
        return SharedPaymentEventDTO.builder()
                .transactionId(transactionId)
                .newStatus(newStatus)
                .timestamp(timestamp)
                .build();
    }
}
