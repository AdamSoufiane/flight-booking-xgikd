package ai.shreds.domain.entities;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.UUID;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ai.shreds.domain.value_objects.DomainValueFlightSchedule;
import ai.shreds.domain.exceptions.DomainException;

/**
 * Domain entity representing a flight schedule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEntityFlightSchedule {

    private UUID flightId;
    private UUID airlineId;
    private UUID aircraftId;
    private String flightNumber;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String origin;
    private String destination;
    private String status;
    private String cancellationReason;
    private LocalDateTime lastUpdated;
    private String aircraftType;
    private int economySeats;
    private int businessSeats;
    private int firstClassSeats;
    private boolean isCodeShare;
    private String operatingAirlineId;
    private String remarks;

    /**
     * Validates the flight schedule.
     *
     * @return true if the schedule is valid
     * @throws DomainException if validation fails
     */
    public boolean validateSchedule() {
        if (flightId == null) {
            throw new DomainException("Flight ID cannot be null");
        }
        if (airlineId == null) {
            throw new DomainException("Airline ID cannot be null");
        }
        if (aircraftId == null) {
            throw new DomainException("Aircraft ID cannot be null");
        }
        if (flightNumber == null || flightNumber.trim().isEmpty()) {
            throw new DomainException("Flight number cannot be null or empty");
        }
        if (departureTime == null || arrivalTime == null) {
            throw new DomainException("Departure and arrival times cannot be null");
        }
        if (!arrivalTime.isAfter(departureTime)) {
            throw new DomainException("Arrival time must be after departure time");
        }
        if (origin == null || origin.trim().isEmpty()) {
            throw new DomainException("Origin cannot be null or empty");
        }
        if (destination == null || destination.trim().isEmpty()) {
            throw new DomainException("Destination cannot be null or empty");
        }
        if (origin.equals(destination)) {
            throw new DomainException("Origin and destination cannot be the same");
        }

        validateFlightDuration();
        validateSeatConfiguration();
        validateCodeShareInfo();

        return true;
    }

    /**
     * Maps the entity to a value object.
     *
     * @return DomainValueFlightSchedule representing this entity
     */
    public DomainValueFlightSchedule mapToValueObject() {
        return DomainValueFlightSchedule.builder()
                .flightId(flightId != null ? flightId.toString() : null)
                .airlineId(airlineId != null ? airlineId.toString() : null)
                .flightNumber(flightNumber)
                .departureTime(departureTime != null ? departureTime.toString() : null)
                .arrivalTime(arrivalTime != null ? arrivalTime.toString() : null)
                .origin(origin)
                .destination(destination)
                .status(status)
                .aircraftType(aircraftType)
                .isCodeShare(isCodeShare)
                .operatingAirlineId(operatingAirlineId)
                .build();
    }

    /**
     * Calculates the flight duration in minutes.
     *
     * @return duration in minutes
     */
    public long getFlightDurationMinutes() {
        if (departureTime == null || arrivalTime == null) {
            return 0;
        }
        return Duration.between(departureTime, arrivalTime).toMinutes();
    }

    /**
     * Checks if the flight is domestic.
     *
     * @return true if origin and destination are in the same country
     */
    public boolean isDomesticFlight() {
        // This is a simplified check. In reality, would need to check against a country database
        return origin.substring(0, 2).equals(destination.substring(0, 2));
    }

    /**
     * Gets the total seat capacity.
     *
     * @return total number of seats
     */
    public int getTotalSeats() {
        return economySeats + businessSeats + firstClassSeats;
    }

    private void validateFlightDuration() {
        long durationMinutes = getFlightDurationMinutes();
        if (durationMinutes < 30) { // Minimum flight time
            throw new DomainException("Flight duration must be at least 30 minutes");
        }
        if (durationMinutes > 1200) { // Maximum flight time (20 hours)
            throw new DomainException("Flight duration cannot exceed 20 hours");
        }
    }

    private void validateSeatConfiguration() {
        if (economySeats < 0 || businessSeats < 0 || firstClassSeats < 0) {
            throw new DomainException("Seat counts cannot be negative");
        }
        if (getTotalSeats() == 0) {
            throw new DomainException("Flight must have at least one seat");
        }
    }

    private void validateCodeShareInfo() {
        if (isCodeShare && (operatingAirlineId == null || operatingAirlineId.trim().isEmpty())) {
            throw new DomainException("Operating airline ID must be specified for code share flights");
        }
    }
}
