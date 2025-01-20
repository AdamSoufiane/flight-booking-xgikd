package ai.shreds.domain.entities;

import ai.shreds.shared.dtos.SharedFlightDTO;
import ai.shreds.shared.dtos.SharedSeatAvailabilityDTO;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEntityFlight {

    private UUID flightId;
    private UUID airlineId;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String origin;
    private String destination;
    
    @Builder.Default
    private List<DomainEntitySeatAvailability> seatAvailability = new ArrayList<>();

    /**
     * Checks if there are available seats for the specified class.
     */
    public boolean hasAvailabilityForClass(String seatClass) {
        return seatAvailability.stream()
                .filter(seat -> seat.getSeatClass().equals(seatClass))
                .anyMatch(seat -> seat.getAvailableSeats() > 0);
    }

    /**
     * Gets the number of available seats for the specified class.
     */
    public int getAvailableSeatsForClass(String seatClass) {
        return seatAvailability.stream()
                .filter(seat -> seat.getSeatClass().equals(seatClass))
                .mapToInt(DomainEntitySeatAvailability::getAvailableSeats)
                .findFirst()
                .orElse(0);
    }

    /**
     * Gets the duration of the flight.
     */
    public Duration getFlightDuration() {
        return Duration.between(departureTime, arrivalTime);
    }

    /**
     * Adds seat availability information to the flight.
     */
    public void addSeatAvailability(DomainEntitySeatAvailability availability) {
        if (availability != null) {
            this.seatAvailability.add(availability);
        }
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
    }

    /**
     * Converts the domain entity to a DTO.
     */
    public SharedFlightDTO toSharedFlightDTO() {
        List<SharedSeatAvailabilityDTO> sharedSeatAvailability = seatAvailability.stream()
                .map(DomainEntitySeatAvailability::toSharedSeatAvailabilityDTO)
                .collect(Collectors.toList());

        return SharedFlightDTO.builder()
                .flightId(flightId)
                .airlineId(airlineId)
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .origin(origin)
                .destination(destination)
                .seatAvailability(sharedSeatAvailability)
                .build();
    }
}