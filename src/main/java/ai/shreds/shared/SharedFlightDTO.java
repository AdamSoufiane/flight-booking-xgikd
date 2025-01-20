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
import java.util.*;

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

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Gets the duration of the flight.
     */
    public Duration getFlightDuration() {
        return Duration.between(departureTime, arrivalTime);
    }

    /**
     * Checks if there are available seats for the specified class.
     */
    public boolean hasAvailabilityForClass(String seatClass) {
        return seatAvailability.stream()
                .filter(seat -> seat.getSeatClass().equals(seatClass))
                .anyMatch(SharedSeatAvailabilityDTO::hasAvailability);
    }

    /**
     * Gets the number of available seats for the specified class.
     */
    public int getAvailableSeatsForClass(String seatClass) {
        return seatAvailability.stream()
                .filter(seat -> seat.getSeatClass().equals(seatClass))
                .mapToInt(SharedSeatAvailabilityDTO::getAvailableSeats)
                .findFirst()
                .orElse(0);
    }

    /**
     * Gets seat availability information for a specific class.
     */
    public Optional<SharedSeatAvailabilityDTO> getSeatAvailability(String seatClass) {
        return seatAvailability.stream()
                .filter(seat -> seat.getSeatClass().equals(seatClass))
                .findFirst();
    }

    /**
     * Checks if the flight is delayed.
     */
    public boolean isDelayed() {
        return "DELAYED".equals(status);
    }

    /**
     * Creates a new instance with updated seat availability.
     */
    public SharedFlightDTO withUpdatedAvailability(List<SharedSeatAvailabilityDTO> updatedAvailability) {
        return SharedFlightDTO.builder()
                .flightId(flightId)
                .airlineId(airlineId)
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .origin(origin)
                .destination(destination)
                .seatAvailability(updatedAvailability)
                .status(status)
                .terminal(terminal)
                .gate(gate)
                .metadata(new HashMap<>(metadata))
                .build();
    }

    /**
     * Validates the flight data.
     */
    public void validate() {
        if (flightId == null) {
            throw new IllegalStateException("Flight ID cannot be null");
        }
        if (airlineId == null) {
            throw new IllegalStateException("Airline ID cannot be null");
        }
        if (departureTime == null) {
            throw new IllegalStateException("Departure time cannot be null");
        }
        if (arrivalTime == null) {
            throw new IllegalStateException("Arrival time cannot be null");
        }
        if (origin == null || origin.trim().isEmpty()) {
            throw new IllegalStateException("Origin cannot be null or empty");
        }
        if (destination == null || destination.trim().isEmpty()) {
            throw new IllegalStateException("Destination cannot be null or empty");
        }
        if (arrivalTime.isBefore(departureTime)) {
            throw new IllegalStateException("Arrival time cannot be before departure time");
        }
        if (seatAvailability != null) {
            seatAvailability.forEach(SharedSeatAvailabilityDTO::validateSeatClass);
        }
    }
}