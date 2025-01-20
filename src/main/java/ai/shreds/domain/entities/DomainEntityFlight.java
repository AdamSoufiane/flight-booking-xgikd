package ai.shreds.domain.entities;

import ai.shreds.shared.dtos.SharedFlightDTO;
import ai.shreds.shared.dtos.SharedSeatAvailabilityDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Domain entity representing a flight with its associated seat availability.
 * Contains business logic for flight management and validation.
 */
@Getter
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
     * Adds seat availability to the flight.
     * @param availability the seat availability to add
     */
    public void addSeatAvailability(DomainEntitySeatAvailability availability) {
        if (availability == null) {
            throw new IllegalArgumentException("Seat availability cannot be null");
        }
        availability.validate();
        this.seatAvailability.add(availability);
    }

    /**
     * Gets the flight duration.
     * @return Duration of the flight
     */
    public Duration getFlightDuration() {
        return Duration.between(departureTime, arrivalTime);
    }

    /**
     * Checks if seats are available for a specific class.
     * @param seatClass the class to check
     * @return true if seats are available
     */
    public boolean hasAvailabilityForClass(String seatClass) {
        return seatAvailability.stream()
                .filter(seat -> seat.getSeatClass().equals(seatClass))
                .anyMatch(DomainEntitySeatAvailability::hasAvailability);
    }

    /**
     * Gets available seats for a specific class.
     * @param seatClass the class to check
     * @return number of available seats
     */
    public int getAvailableSeatsForClass(String seatClass) {
        return seatAvailability.stream()
                .filter(seat -> seat.getSeatClass().equals(seatClass))
                .findFirst()
                .map(DomainEntitySeatAvailability::getAvailableSeats)
                .orElse(0);
    }

    /**
     * Updates seat availability for a specific class.
     * @param seatClass the class to update
     * @param newCount the new seat count
     */
    public void updateSeatAvailability(String seatClass, int newCount) {
        seatAvailability.stream()
                .filter(seat -> seat.getSeatClass().equals(seatClass))
                .findFirst()
                .ifPresent(seat -> seat.updateAvailableSeats(newCount));
    }

    /**
     * Validates the entity state.
     * @throws IllegalStateException if the entity is in an invalid state
     */
    public void validate() {
        if (flightId == null) {
            throw new IllegalStateException("Flight ID is required");
        }
        if (airlineId == null) {
            throw new IllegalStateException("Airline ID is required");
        }
        if (departureTime == null) {
            throw new IllegalStateException("Departure time is required");
        }
        if (arrivalTime == null) {
            throw new IllegalStateException("Arrival time is required");
        }
        if (arrivalTime.isBefore(departureTime) || arrivalTime.equals(departureTime)) {
            throw new IllegalStateException("Arrival time must be after departure time");
        }
        if (origin == null || origin.trim().isEmpty()) {
            throw new IllegalStateException("Origin is required");
        }
        if (destination == null || destination.trim().isEmpty()) {
            throw new IllegalStateException("Destination is required");
        }
        if (origin.equals(destination)) {
            throw new IllegalStateException("Origin and destination cannot be the same");
        }
        
        // Validate all seat availability entries
        seatAvailability.forEach(DomainEntitySeatAvailability::validate);
    }

    /**
     * Converts this domain entity to a shared DTO.
     * @return SharedFlightDTO representation
     */
    public SharedFlightDTO toSharedFlightDTO() {
        List<SharedSeatAvailabilityDTO> seatDTOs = seatAvailability.stream()
                .map(DomainEntitySeatAvailability::toSharedSeatAvailabilityDTO)
                .collect(Collectors.toList());

        return SharedFlightDTO.builder()
                .flightId(this.flightId)
                .airlineId(this.airlineId)
                .departureTime(this.departureTime)
                .arrivalTime(this.arrivalTime)
                .origin(this.origin)
                .destination(this.destination)
                .seatAvailability(seatDTOs)
                .build();
    }
}
