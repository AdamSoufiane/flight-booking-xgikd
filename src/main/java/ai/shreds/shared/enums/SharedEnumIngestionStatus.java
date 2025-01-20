package ai.shreds.shared.enums;

/**
 * Enumeration of possible ingestion operation statuses.
 */
public enum SharedEnumIngestionStatus {
    SUCCESS("SUCCESS"),
    PARTIAL_SUCCESS("PARTIAL_SUCCESS"),
    FAILED("FAILED"),
    IN_PROGRESS("IN_PROGRESS");

    private final String value;

    SharedEnumIngestionStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SharedEnumIngestionStatus fromValue(String value) {
        for (SharedEnumIngestionStatus status : SharedEnumIngestionStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}