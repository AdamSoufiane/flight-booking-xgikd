package ai.shreds.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object representing error responses from the payment service.
 * Can contain either general error information or fraud-specific alerts.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SharedErrorResponseDTO {

    @JsonProperty("error")
    private String error;

    @JsonProperty("fraud_alert")
    private String fraudAlert;

    /**
     * Creates an error response with a general error message
     * @param message The error message
     * @return SharedErrorResponseDTO containing the error message
     */
    public static SharedErrorResponseDTO error(String message) {
        return SharedErrorResponseDTO.builder()
                .error(message)
                .build();
    }

    /**
     * Creates an error response with a fraud alert message
     * @param message The fraud alert message
     * @return SharedErrorResponseDTO containing the fraud alert
     */
    public static SharedErrorResponseDTO fraudAlert(String message) {
        return SharedErrorResponseDTO.builder()
                .fraudAlert(message)
                .build();
    }

    /**
     * Checks if this response contains a fraud alert
     * @return true if this is a fraud alert, false otherwise
     */
    public boolean isFraudAlert() {
        return fraudAlert != null && !fraudAlert.isEmpty();
    }

    /**
     * Checks if this response contains an error
     * @return true if this is an error response, false otherwise
     */
    public boolean isError() {
        return error != null && !error.isEmpty();
    }
}
