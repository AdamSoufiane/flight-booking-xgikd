package ai.shreds.domain.entities;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing the status of a flight data ingestion operation.
 */
@Data
@Builder
public class DomainEntityIngestionStatus {
    private UUID ingestionId;
    private List<UUID> airlineIds;
    private LocalDateTime startTime;
    private LocalDateTime lastUpdated;
    private String status;
    private String message;
    private int totalRecords;
    private int processedRecords;
    private List<String> errors;

    /**
     * Calculates the progress percentage of the ingestion.
     */
    public int getProgressPercentage() {
        if (totalRecords == 0) return 0;
        return (int) ((processedRecords * 100.0) / totalRecords);
    }

    /**
     * Checks if the ingestion is complete.
     */
    public boolean isComplete() {
        return "COMPLETED".equals(status) || "FAILED".equals(status);
    }

    /**
     * Updates the status with new information.
     */
    public void updateProgress(int processed, String message) {
        this.processedRecords = processed;
        this.message = message;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Marks the ingestion as complete.
     */
    public void markComplete(String message) {
        this.status = "COMPLETED";
        this.message = message;
        this.lastUpdated = LocalDateTime.now();
        this.processedRecords = this.totalRecords;
    }

    /**
     * Marks the ingestion as failed.
     */
    public void markFailed(String error) {
        this.status = "FAILED";
        this.message = error;
        this.lastUpdated = LocalDateTime.now();
        if (this.errors == null) {
            this.errors = List.of(error);
        } else {
            this.errors.add(error);
        }
    }
}