package ai.shreds.shared.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO representing seat availability for a specific flight and seat class.
 * Includes validation to ensure data integrity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedSeatAvailabilityDTO {

    private static final Set<String> VALID_SEAT_CLASSES = Set.of(
            "ECONOMY", "PREMIUM_ECONOMY", "BUSINESS", "FIRST");

    @NotBlank(message = "Seat class is required")
    @Pattern(regexp = "^(ECONOMY|PREMIUM_ECONOMY|BUSINESS|FIRST)$",
            message = "Invalid seat class. Must be one of: ECONOMY, PREMIUM_ECONOMY, BUSINESS, FIRST")
    private String seatClass;

    @NotNull(message = "Available seats count is required")
    @Min(value = 0, message = "Available seats cannot be negative")
    private Integer availableSeats;

    /**
     * Checks if seats are available for this class.
     * @return true if there are available seats, false otherwise
     */
    public boolean hasAvailability() {
        return availableSeats != null && availableSeats > 0;
    }

    /**
     * Checks if specific number of seats are available.
     * @param requiredSeats number of seats needed
     * @return true if enough seats are available
     */
    public boolean hasAvailability(int requiredSeats) {
        return availableSeats != null && availableSeats >= requiredSeats;
    }

    /**
     * Creates a copy of this DTO with updated seat count.
     * @param newSeatCount the new number of available seats
     * @return new DTO instance with updated seat count
     * @throws IllegalArgumentException if new count is negative
     */
    public SharedSeatAvailabilityDTO withUpdatedSeatCount(int newSeatCount) {
        if (newSeatCount < 0) {
            throw new IllegalArgumentException("Seat count cannot be negative");
        }
        return SharedSeatAvailabilityDTO.builder()
                .seatClass(this.seatClass)
                .availableSeats(newSeatCount)
                .build();
    }

    /**
     * Validates the seat class value.
     * @throws IllegalArgumentException if seat class is invalid
     */
    public void validateSeatClass() {
        if (seatClass == null || seatClass.trim().isEmpty()) {
            throw new IllegalArgumentException("Seat class is required");
        }
        if (!VALID_SEAT_CLASSES.contains(seatClass.toUpperCase())) {
            throw new IllegalArgumentException(
                    "Invalid seat class. Must be one of: " + String.join(", ", VALID_SEAT_CLASSES));
        }
    }

    /**
     * Gets the normalized (uppercase) seat class.
     * @return normalized seat class
     */
    public String getNormalizedSeatClass() {
        return seatClass != null ? seatClass.toUpperCase() : null;
    }

    /**
     * Creates a copy with decremented seat count.
     * @param count number of seats to remove
     * @return new DTO with updated count
     * @throws IllegalArgumentException if count is invalid
     */
    public SharedSeatAvailabilityDTO withDecrementedSeats(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Decrement count cannot be negative");
        }
        if (count > availableSeats) {
            throw new IllegalArgumentException("Not enough seats available");
        }
        return withUpdatedSeatCount(availableSeats - count);
    }
}
