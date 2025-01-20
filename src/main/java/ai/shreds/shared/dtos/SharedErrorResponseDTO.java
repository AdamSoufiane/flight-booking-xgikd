package ai.shreds.shared.dtos;

import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * Standardized error response DTO for API errors.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SharedErrorResponseDTO {
    private String status;
    private String message;
    private String code;
    private String path;
    private LocalDateTime timestamp;
    private String details;

    /**
     * Creates a basic error response.
     */
    public static SharedErrorResponseDTO of(String message) {
        return SharedErrorResponseDTO.builder()
                .status("ERROR")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a detailed error response.
     */
    public static SharedErrorResponseDTO of(String message, String code, String details) {
        return SharedErrorResponseDTO.builder()
                .status("ERROR")
                .message(message)
                .code(code)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }
}