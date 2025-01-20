package ai.shreds.shared.dtos;

import ai.shreds.application.dtos.ApplicationSearchRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for flight search request.
 * Contains validated search parameters for flight searches.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedFlightSearchRequest {

    private static final Set<String> VALID_SEAT_CLASSES = Set.of(
            "ECONOMY", "PREMIUM_ECONOMY", "BUSINESS", "FIRST");

    @NotBlank(message = "Origin airport code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Origin must be a valid 3-letter IATA airport code")
    private String origin;

    @NotBlank(message = "Destination airport code is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Destination must be a valid 3-letter IATA airport code")
    private String destination;

    @NotNull(message = "Departure date is required")
    @FutureOrPresent(message = "Departure date must be in the present or future")
    private LocalDateTime departureDate;

    @FutureOrPresent(message = "Return date must be in the present or future")
    private LocalDateTime returnDate;

    private String seatClass;

    @Builder.Default
    private int maxConnections = 0;

    @Builder.Default
    private int minConnectionTime = 30; // minutes

    @Builder.Default
    private int maxConnectionTime = 720; // minutes (12 hours)

    /**
     * Validates all business rules for the search request.
     * @throws IllegalArgumentException if any business rule is violated
     */
    public void validateBusinessRules() {
        validateAirportCodes();
        validateDates();
        validateSeatClass();
        validateConnectionTimes();
    }

    private void validateAirportCodes() {
        if (origin.equals(destination)) {
            throw new IllegalArgumentException("Origin and destination cannot be the same");
        }
    }

    private void validateDates() {
        if (returnDate != null) {
            if (returnDate.isBefore(departureDate)) {
                throw new IllegalArgumentException("Return date must be after departure date");
            }
            long daysBetween = ChronoUnit.DAYS.between(departureDate, returnDate);
            if (daysBetween > 365) {
                throw new IllegalArgumentException("Trip duration cannot exceed 365 days");
            }
        }
    }

    private void validateSeatClass() {
        if (seatClass != null && !seatClass.isEmpty()) {
            String normalizedSeatClass = seatClass.toUpperCase();
            if (!VALID_SEAT_CLASSES.contains(normalizedSeatClass)) {
                throw new IllegalArgumentException(
                        "Invalid seat class. Must be one of: " + String.join(", ", VALID_SEAT_CLASSES));
            }
        }
    }

    private void validateConnectionTimes() {
        if (maxConnections < 0) {
            throw new IllegalArgumentException("Maximum connections cannot be negative");
        }
        if (minConnectionTime < 30) {
            throw new IllegalArgumentException("Minimum connection time must be at least 30 minutes");
        }
        if (maxConnectionTime > 720) {
            throw new IllegalArgumentException("Maximum connection time cannot exceed 12 hours");
        }
        if (minConnectionTime >= maxConnectionTime) {
            throw new IllegalArgumentException("Minimum connection time must be less than maximum connection time");
        }
    }

    /**
     * Converts this DTO to an application-specific search request.
     * @return ApplicationSearchRequest with all necessary fields populated
     * @throws IllegalArgumentException if business rules are violated
     */
    public ApplicationSearchRequest toApplicationSearchRequest() {
        validateBusinessRules();
        
        return ApplicationSearchRequest.builder()
                .origin(this.origin.toUpperCase())
                .destination(this.destination.toUpperCase())
                .departureDate(this.departureDate)
                .returnDate(this.returnDate)
                .seatClass(this.seatClass != null ? this.seatClass.toUpperCase() : null)
                .isRoundTrip(this.returnDate != null)
                .searchId(UUID.randomUUID().toString())
                .searchTimestamp(LocalDateTime.now())
                .maxConnections(this.maxConnections)
                .minConnectionTime(this.minConnectionTime)
                .maxConnectionTime(this.maxConnectionTime)
                .build();
    }

    /**
     * Checks if this is a round trip search.
     * @return true if return date is specified
     */
    public boolean isRoundTrip() {
        return returnDate != null;
    }

    /**
     * Gets the normalized seat class.
     * @return uppercase seat class or null if not specified
     */
    public String getNormalizedSeatClass() {
        return seatClass != null ? seatClass.toUpperCase() : null;
    }
}
