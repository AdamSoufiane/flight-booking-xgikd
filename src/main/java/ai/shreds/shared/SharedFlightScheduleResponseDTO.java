package ai.shreds.shared.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Data Transfer Object representing a flight schedule in the response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SharedFlightScheduleResponseDTO {
    
    /**
     * Unique identifier for the flight.
     */
    private String flightId;

    /**
     * Flight number.
     */
    private String flightNumber;

    /**
     * Identifier of the airline operating the flight.
     */
    private String airlineId;

    /**
     * Name of the airline operating the flight.
     */
    private String airlineName;

    /**
     * Scheduled departure time in ISO-8601 format.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private String departureTime;

    /**
     * Scheduled arrival time in ISO-8601 format.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private String arrivalTime;

    /**
     * IATA/ICAO code of the origin airport.
     */
    private String origin;

    /**
     * Name of the origin airport.
     */
    private String originAirportName;

    /**
     * Terminal at origin airport.
     */
    private String originTerminal;

    /**
     * IATA/ICAO code of the destination airport.
     */
    private String destination;

    /**
     * Name of the destination airport.
     */
    private String destinationAirportName;

    /**
     * Terminal at destination airport.
     */
    private String destinationTerminal;

    /**
     * Flight duration in minutes.
     */
    private Integer durationMinutes;

    /**
     * Aircraft type.
     */
    private String aircraftType;

    /**
     * Aircraft registration.
     */
    private String aircraftRegistration;

    /**
     * Flight status.
     */
    private String status;

    /**
     * Basic economy seat availability.
     */
    private Integer basicEconomyAvailable;

    /**
     * Business class seat availability.
     */
    private Integer businessClassAvailable;

    /**
     * First class seat availability.
     */
    private Integer firstClassAvailable;

    /**
     * Basic economy fare.
     */
    private Double basicEconomyFare;

    /**
     * Business class fare.
     */
    private Double businessClassFare;

    /**
     * First class fare.
     */
    private Double firstClassFare;

    /**
     * Whether this is a codeshare flight.
     */
    private Boolean isCodeshare;

    /**
     * Operating airline ID for codeshare flights.
     */
    private String operatingAirlineId;

    /**
     * Operating airline name for codeshare flights.
     */
    private String operatingAirlineName;

    /**
     * Meal service information.
     */
    private String mealService;

    /**
     * Additional remarks.
     */
    private String remarks;

    /**
     * Baggage allowance information.
     */
    private Map<String, String> baggageAllowance;

    /**
     * Fare rules and conditions.
     */
    private Map<String, Object> fareRules;

    /**
     * Calculates and returns the flight duration in a human-readable format.
     */
    public String getFormattedDuration() {
        if (durationMinutes == null) return null;
        int hours = durationMinutes / 60;
        int minutes = durationMinutes % 60;
        return String.format("%dh %dm", hours, minutes);
    }

    /**
     * Checks if the flight has any available seats in any class.
     */
    public boolean hasAvailableSeats() {
        return (basicEconomyAvailable != null && basicEconomyAvailable > 0) ||
               (businessClassAvailable != null && businessClassAvailable > 0) ||
               (firstClassAvailable != null && firstClassAvailable > 0);
    }

    /**
     * Gets the lowest available fare across all classes.
     */
    public Double getLowestFare() {
        Double lowest = null;
        if (basicEconomyFare != null && basicEconomyAvailable > 0) {
            lowest = basicEconomyFare;
        }
        if (businessClassFare != null && businessClassAvailable > 0) {
            lowest = (lowest == null) ? businessClassFare : Math.min(lowest, businessClassFare);
        }
        if (firstClassFare != null && firstClassAvailable > 0) {
            lowest = (lowest == null) ? firstClassFare : Math.min(lowest, firstClassFare);
        }
        return lowest;
    }

    /**
     * Gets total available seats across all classes.
     */
    public int getTotalAvailableSeats() {
        int total = 0;
        if (basicEconomyAvailable != null) total += basicEconomyAvailable;
        if (businessClassAvailable != null) total += businessClassAvailable;
        if (firstClassAvailable != null) total += firstClassAvailable;
        return total;
    }

    /**
     * Checks if this is an international flight.
     */
    public boolean isInternationalFlight() {
        if (origin == null || destination == null) return false;
        return !origin.substring(0, 2).equals(destination.substring(0, 2));
    }

    /**
     * Gets a formatted string representation of the flight.
     */
    public String getFormattedFlight() {
        StringBuilder sb = new StringBuilder()
                .append(flightNumber)
                .append(": ")
                .append(origin)
                .append("(")
                .append(originTerminal != null ? originTerminal : "-")
                .append(") → ")
                .append(destination)
                .append("(")
                .append(destinationTerminal != null ? destinationTerminal : "-")
                .append(")");

        if (status != null) {
            sb.append(" [")
              .append(status)
              .append("]");
        }

        return sb.toString();
    }
}
