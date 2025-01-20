package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedEnumIngestionStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object representing the response of a flight data ingestion operation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SharedIngestionResponseDTO {
    
    /**
     * Status of the ingestion operation.
     */
    private SharedEnumIngestionStatus status;

    /**
     * Detailed message about the ingestion operation result.
     */
    private String message;

    /**
     * List of airline IDs that were successfully processed.
     */
    private List<String> ingestedAirlines;

    /**
     * Unique identifier for this ingestion operation.
     */
    private String ingestionId;

    /**
     * Time when the ingestion started.
     */
    private LocalDateTime startTime;

    /**
     * Time of the last status update.
     */
    private LocalDateTime lastUpdated;

    /**
     * Progress percentage of the ingestion.
     */
    private Integer progressPercentage;

    /**
     * Total number of records to process.
     */
    private Integer totalRecords;

    /**
     * Number of records processed so far.
     */
    private Integer processedRecords;

    /**
     * Number of records successfully processed.
     */
    private Integer successfulRecords;

    /**
     * Number of records that failed processing.
     */
    private Integer failedRecords;

    /**
     * List of any errors encountered during ingestion.
     */
    private List<String> errors;

    /**
     * Detailed statistics about the ingestion process.
     */
    private Map<String, Object> statistics;

    /**
     * Factory method to create a success response.
     */
    public static SharedIngestionResponseDTO success(String message, List<String> ingestedAirlines) {
        return SharedIngestionResponseDTO.builder()
                .status(SharedEnumIngestionStatus.SUCCESS)
                .message(message)
                .ingestedAirlines(ingestedAirlines)
                .lastUpdated(LocalDateTime.now())
                .progressPercentage(100)
                .build();
    }

    /**
     * Factory method to create a failure response.
     */
    public static SharedIngestionResponseDTO failure(String message, List<String> errors) {
        return SharedIngestionResponseDTO.builder()
                .status(SharedEnumIngestionStatus.FAILED)
                .message(message)
                .errors(errors)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method to create a partial success response.
     */
    public static SharedIngestionResponseDTO partialSuccess(
            String message, List<String> ingestedAirlines, List<String> errors) {
        return SharedIngestionResponseDTO.builder()
                .status(SharedEnumIngestionStatus.PARTIAL_SUCCESS)
                .message(message)
                .ingestedAirlines(ingestedAirlines)
                .errors(errors)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method to create an in-progress response.
     */
    public static SharedIngestionResponseDTO inProgress(
            String ingestionId, String message, int progress, int total) {
        return SharedIngestionResponseDTO.builder()
                .status(SharedEnumIngestionStatus.IN_PROGRESS)
                .ingestionId(ingestionId)
                .message(message)
                .progressPercentage(calculateProgress(progress, total))
                .totalRecords(total)
                .processedRecords(progress)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * Checks if the ingestion is complete.
     */
    public boolean isComplete() {
        return status == SharedEnumIngestionStatus.SUCCESS ||
               status == SharedEnumIngestionStatus.FAILED ||
               status == SharedEnumIngestionStatus.PARTIAL_SUCCESS;
    }

    /**
     * Gets a formatted status message.
     */
    public String getFormattedStatus() {
        StringBuilder sb = new StringBuilder()
                .append(status)
                .append(": ")
                .append(message);

        if (progressPercentage != null) {
            sb.append(" (")
              .append(progressPercentage)
              .append("% complete)");
        }

        if (errors != null && !errors.isEmpty()) {
            sb.append("\nErrors encountered: ")
              .append(errors.size());
        }

        return sb.toString();
    }

    private static int calculateProgress(int processed, int total) {
        if (total == 0) return 0;
        return (int) ((processed * 100.0) / total);
    }
}
