package ai.shreds.shared.value_objects;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * Value object representing the request parameters for flight data ingestion.
 * Contains the list of airline IDs to process and the date range for data retrieval.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SharedIngestionRequestParams {
    
    /**
     * List of airline IDs to process for data ingestion.
     * Cannot be empty when specified.
     */
    @NotEmpty(message = "At least one airline ID must be provided")
    private List<String> airlineIds;

    /**
     * Date range for which to ingest flight data.
     * Must follow the format: YYYY-MM-DD/YYYY-MM-DD
     */
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}/\\d{4}-\\d{2}-\\d{2}", 
            message = "Date range must be in format YYYY-MM-DD/YYYY-MM-DD")
    private String dateRange;
}
