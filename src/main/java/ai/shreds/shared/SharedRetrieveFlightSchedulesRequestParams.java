package ai.shreds.shared.value_objects;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Value object representing the request parameters for retrieving flight schedules.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedRetrieveFlightSchedulesRequestParams {
    
    /**
     * IATA/ICAO code of the origin airport.
     */
    @NotBlank(message = "Origin airport code is required")
    @Pattern(regexp = "^[A-Z]{3,4}$", 
            message = "Origin must be a valid IATA (3 letters) or ICAO (4 letters) code")
    private String origin;

    /**
     * IATA/ICAO code of the destination airport.
     */
    @NotBlank(message = "Destination airport code is required")
    @Pattern(regexp = "^[A-Z]{3,4}$", 
            message = "Destination must be a valid IATA (3 letters) or ICAO (4 letters) code")
    private String destination;

    /**
     * Date range for which to retrieve flight schedules.
     */
    @NotBlank(message = "Date range is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}/\\d{4}-\\d{2}-\\d{2}", 
            message = "Date range must be in format YYYY-MM-DD/YYYY-MM-DD")
    private String dateRange;

    /**
     * Maximum number of stops allowed.
     */
    @Min(value = 0, message = "Minimum stops must be 0 or greater")
    @Max(value = 2, message = "Maximum stops cannot exceed 2")
    private Integer maxStops;

    /**
     * Preferred airline codes (IATA).
     */
    @Pattern(regexp = "^([A-Z0-9]{2}(,[A-Z0-9]{2})*)?$", 
            message = "Airline codes must be 2-character IATA codes separated by commas")
    private String preferredAirlines;

    /**
     * Cabin class preference.
     */
    @Pattern(regexp = "^(ECONOMY|PREMIUM_ECONOMY|BUSINESS|FIRST)?$", 
            message = "Invalid cabin class")
    private String cabinClass;

    /**
     * Minimum number of seats required.
     */
    @Min(value = 1, message = "Minimum seats must be at least 1")
    @Max(value = 9, message = "Maximum seats cannot exceed 9")
    private Integer minSeats;

    /**
     * Maximum price limit.
     */
    @Min(value = 0, message = "Price limit cannot be negative")
    private Double maxPrice;

    /**
     * Whether to include codeshare flights.
     */
    private Boolean includeCodeshare;

    /**
     * Whether to include connecting flights.
     */
    private Boolean includeConnections;

    /**
     * Validates that origin and destination are not the same.
     */
    public boolean validateDifferentAirports() {
        return origin == null || destination == null || !origin.equals(destination);
    }

    /**
     * Validates the date range format and logic.
     */
    public boolean validateDateRange() {
        if (dateRange == null) return false;
        
        String[] dates = dateRange.split("/");
        if (dates.length != 2) return false;
        
        try {
            LocalDate startDate = LocalDate.parse(dates[0]);
            LocalDate endDate = LocalDate.parse(dates[1]);
            LocalDate today = LocalDate.now();

            // Validate date range logic
            if (startDate.isBefore(today)) return false;
            if (endDate.isBefore(startDate)) return false;
            if (ChronoUnit.DAYS.between(startDate, endDate) > 30) return false;

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets preferred airlines as a list.
     */
    public List<String> getPreferredAirlinesList() {
        if (preferredAirlines == null || preferredAirlines.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(preferredAirlines.split(","));
    }

    /**
     * Gets the start date from the date range.
     */
    public LocalDate getStartDate() {
        if (!validateDateRange()) return null;
        return LocalDate.parse(dateRange.split("/")[0]);
    }

    /**
     * Gets the end date from the date range.
     */
    public LocalDate getEndDate() {
        if (!validateDateRange()) return null;
        return LocalDate.parse(dateRange.split("/")[1]);
    }

    /**
     * Creates a builder with default values.
     */
    public static SharedRetrieveFlightSchedulesRequestParamsBuilder defaultBuilder() {
        return builder()
                .maxStops(0)
                .includeCodeshare(false)
                .includeConnections(false)
                .minSeats(1)
                .cabinClass("ECONOMY");
    }
}
