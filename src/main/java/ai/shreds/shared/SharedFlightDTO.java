package ai.shreds.shared.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DTO representing detailed flight information including seat availability.
 * Includes validation and utility methods for flight data handling.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedFlightDTO {
    @NotNull(message = "Flight ID is required")
    private UUID flightId;

    @NotNull(message = "Airline ID is required")
    private UUID airlineId;

    @NotNull(message = "Departure time is required")
    @FutureOrPresent(message = "Departure time must be in the present or future")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is required")
    @FutureOrPresent(message = "Arrival time must be in the present or future")
    private LocalDateTime arrivalTime;

    @NotBlank(message = "Origin is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Origin must be a valid 3-letter IATA airport code")
    private String origin;

    @NotBlank(message = "Destination is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Destination must be a valid 3-letter IATA airport code")
    private String destination;

    @Valid
    @Builder.Default
    private List<SharedSeatAvailabilityDTO> seatAvailability = new ArrayList<>();

    @Builder.Default
    private String status = "SCHEDULED";

    private String terminal;
    private String gate;

    /**
     * Calculates the flight duration.
     * @return Duration of the flight
     */
    public Duration getFlightDuration() {
        return Duration.between(departureTime, arrivalTime);
    }

    /**
     * Checks if seats are available for a specific class.
     * @param seatClass the class to check availability for
     * @return true if seats are available, false otherwise
     */
    public boolean hasAvailabilityForClass(String seatClass) {
        return seatAvailability.stream()
                .filter(seat -> seat.getSeatClass().equalsIgnoreCase(seatClass))
                .anyMatch(SharedSeatAvailabilityDTO::hasAvailability);
    }

    /**
     * Gets the number of available seats for a specific class.
     * @param seatClass the class to check availability for
     * @return number of available seats, 0 if none available
     */
    public int getAvailableSeatsForClass(String seatClass) {
        return seatAvailability.stream()
                .filter(seat -> seat.getSeatClass().equalsIgnoreCase(seatClass))
                .findFirst()
                .map(SharedSeatAvailabilityDTO::getAvailableSeats)
                .orElse(0);
    }

    /**
     * Gets seat availability for a specific class.
     * @param seatClass the class to get availability for
     * @return optional containing seat availability if found
     */
    public Optional<SharedSeatAvailabilityDTO> getSeatAvailability(String seatClass) {
        return seatAvailability.stream()
                .filter(seat -> seat.getSeatClass().equalsIgnoreCase(seatClass))
                .findFirst();
    }

    /**
     * Validates all aspects of the flight data.
     * @throws IllegalStateException if validation fails
     */
    public void validate() {
        validateTimes();
        validateAirportCodes();
        validateSeatAvailability();
    }

    /**
     * Validates that arrival time is after departure time.
     * @throws IllegalStateException if arrival time is before or equal to departure time
     */
    private void validateTimes() {
        if (arrivalTime.isBefore(departureTime) || arrivalTime.equals(departureTime)) {
            throw new IllegalStateException("Arrival time must be after departure time");
        }
    }

    /**
     * Validates airport codes.
     * @throws IllegalStateException if airport codes are invalid
     */
    private void validateAirportCodes() {
        if (origin.equals(destination)) {
            throw new IllegalStateException("Origin and destination cannot be the same");
        }
    }

    /**
     * Validates seat availability data.
     * @throws IllegalStateException if seat availability is invalid
     */
    private void validateSeatAvailability() {
        if (seatAvailability == null) {
            throw new IllegalStateException("Seat availability cannot be null");
        }
        seatAvailability.forEach(SharedSeatAvailabilityDTO::validateSeatClass);
    }

    /**
     * Checks if the flight is delayed.
     * @return true if flight status indicates a delay
     */
    public boolean isDelayed() {
        return "DELAYED".equals(status);
    }

    /**
     * Creates a copy of this DTO with updated seat availability.
     * @param updatedAvailability new seat availability list
     * @return new DTO instance with updated availability
     */
    public SharedFlightDTO withUpdatedAvailability(List<SharedSeatAvailabilityDTO> updatedAvailability) {
        return SharedFlightDTO.builder()
                .flightId(this.flightId)
                .airlineId(this.airlineId)
                .departureTime(this.departureTime)
                .arrivalTime(this.arrivalTime)
                .origin(this.origin)
                .destination(this.destination)
                .seatAvailability(new ArrayList<>(updatedAvailability))
                .status(this.status)
                .terminal(this.terminal)
                .gate(this.gate)
                .build();
    }
}
