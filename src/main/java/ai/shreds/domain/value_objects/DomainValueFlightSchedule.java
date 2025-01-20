package ai.shreds.domain.value_objects;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Value object representing a flight schedule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainValueFlightSchedule {
    private String flightId;
    private String airlineId;
    private String flightNumber;
    private String departureTime;
    private String arrivalTime;
    private String origin;
    private String destination;
    private String status;
    private String aircraftType;
    private boolean isCodeShare;
    private String operatingAirlineId;
    private String duration;
    private String originTerminal;
    private String destinationTerminal;
    private String mealService;
    private String aircraftRegistration;
    private String remarks;
    
    /**
     * Creates a formatted string representation of the flight schedule.
     *
     * @return Formatted string with key flight information
     */
    public String toFormattedString() {
        return String.format("%s: %s-%s (%s to %s)", 
                flightNumber, 
                origin, 
                destination, 
                departureTime, 
                arrivalTime);
    }

    /**
     * Checks if this is a domestic flight based on origin and destination.
     *
     * @return true if origin and destination are in the same country
     */
    public boolean isDomesticFlight() {
        // This is a simplified check. In reality, would need to check against a country database
        return origin != null && destination != null && 
               origin.substring(0, 2).equals(destination.substring(0, 2));
    }
}
