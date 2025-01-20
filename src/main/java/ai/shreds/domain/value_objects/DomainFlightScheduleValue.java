package ai.shreds.domain.value_objects;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Value object representing a flight schedule in the domain layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainFlightScheduleValue {
    private String flightId;
    private String airlineId;
    private String flightNumber;
    private String departureTime;
    private String arrivalTime;
    private String origin;
    private String destination;
    private String status;
    private String aircraftType;
    private String originTerminal;
    private String destinationTerminal;
    private String aircraftRegistration;
    private String operatingAirlineId;
    private boolean isCodeShare;
    private String mealService;
    private String remarks;
    private int economySeats;
    private int businessSeats;
    private int firstClassSeats;

    /**
     * Validates the basic structure of the flight schedule.
     *
     * @return true if the schedule is valid
     */
    public boolean isValid() {
        return flightId != null && !flightId.trim().isEmpty() &&
               airlineId != null && !airlineId.trim().isEmpty() &&
               flightNumber != null && !flightNumber.trim().isEmpty() &&
               departureTime != null && !departureTime.trim().isEmpty() &&
               arrivalTime != null && !arrivalTime.trim().isEmpty() &&
               origin != null && !origin.trim().isEmpty() &&
               destination != null && !destination.trim().isEmpty() &&
               status != null && !status.trim().isEmpty();
    }

    /**
     * Checks if this is a domestic flight based on origin and destination.
     *
     * @return true if origin and destination are in the same country
     */
    public boolean isDomesticFlight() {
        return origin != null && destination != null && 
               origin.substring(0, 2).equals(destination.substring(0, 2));
    }

    /**
     * Gets a formatted string representation of the flight schedule.
     *
     * @return Formatted string with key flight information
     */
    public String getFormattedSchedule() {
        return String.format("%s: %s-%s (%s to %s)", 
                flightNumber, 
                origin, 
                destination, 
                departureTime, 
                arrivalTime);
    }

    /**
     * Gets the total seat capacity across all classes.
     *
     * @return Total number of seats
     */
    public int getTotalSeats() {
        return economySeats + businessSeats + firstClassSeats;
    }
}
