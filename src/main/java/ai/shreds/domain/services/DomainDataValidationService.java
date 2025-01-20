package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainEntityAirlineInfo;
import ai.shreds.domain.entities.DomainEntityFlightSchedule;
import ai.shreds.domain.entities.DomainEntitySeatAvailability;
import ai.shreds.domain.exceptions.DomainException;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.regex.Pattern;

/**
 * Domain service responsible for validating various domain entities.
 * Implements business rules and validation logic for flight-related data.
 */
public class DomainDataValidationService {

    private static final int MAX_FLIGHT_DURATION_HOURS = 20;
    private static final Pattern AIRPORT_CODE_PATTERN = Pattern.compile("^[A-Z]{3,4}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    /**
     * Validates a flight schedule entity.
     *
     * @param flight The flight schedule to validate
     * @return true if validation passes
     * @throws DomainException if validation fails
     */
    public boolean validateFlightSchedule(DomainEntityFlightSchedule flight) {
        if (flight == null) {
            throw new DomainException("Flight schedule cannot be null");
        }

        // Validate IDs
        if (flight.getFlightId() == null) {
            throw new DomainException("Flight ID cannot be null");
        }
        if (flight.getAirlineId() == null) {
            throw new DomainException("Airline ID cannot be null");
        }

        // Validate airports
        validateAirportCode(flight.getOrigin(), "Origin");
        validateAirportCode(flight.getDestination(), "Destination");
        if (flight.getOrigin().equals(flight.getDestination())) {
            throw new DomainException("Origin and destination airports cannot be the same");
        }

        // Validate times
        validateFlightTimes(flight.getDepartureTime(), flight.getArrivalTime());

        return true;
    }

    /**
     * Validates seat availability entity.
     *
     * @param seats The seat availability to validate
     * @return true if validation passes
     * @throws DomainException if validation fails
     */
    public boolean validateSeatAvailability(DomainEntitySeatAvailability seats) {
        if (seats == null) {
            throw new DomainException("Seat availability cannot be null");
        }

        if (seats.getFlightId() == null) {
            throw new DomainException("Flight ID cannot be null in seat availability");
        }

        if (seats.getSeatClass() == null || seats.getSeatClass().trim().isEmpty()) {
            throw new DomainException("Seat class must be specified");
        }

        if (seats.getTotalSeats() < 0) {
            throw new DomainException("Total seats cannot be negative");
        }

        if (seats.getAvailableSeats() < 0 || seats.getAvailableSeats() > seats.getTotalSeats()) {
            throw new DomainException("Available seats must be between 0 and total seats");
        }

        return true;
    }

    /**
     * Validates airline information entity.
     *
     * @param entity The airline information to validate
     * @return true if validation passes
     * @throws DomainException if validation fails
     */
    public boolean validateAirlineInfo(DomainEntityAirlineInfo entity) {
        if (entity == null) {
            throw new DomainException("Airline information cannot be null");
        }

        if (entity.getAirlineId() == null) {
            throw new DomainException("Airline ID cannot be null");
        }

        if (entity.getAirlineName() == null || entity.getAirlineName().trim().isEmpty()) {
            throw new DomainException("Airline name cannot be null or empty");
        }

        validateContactDetails(entity.getContactDetails());

        return true;
    }

    private void validateAirportCode(String code, String fieldName) {
        if (code == null || code.trim().isEmpty()) {
            throw new DomainException(fieldName + " airport code cannot be null or empty");
        }
        if (!AIRPORT_CODE_PATTERN.matcher(code).matches()) {
            throw new DomainException(fieldName + " must be a valid IATA (3 letters) or ICAO (4 letters) code");
        }
    }

    private void validateFlightTimes(LocalDateTime departure, LocalDateTime arrival) {
        if (departure == null) {
            throw new DomainException("Departure time cannot be null");
        }
        if (arrival == null) {
            throw new DomainException("Arrival time cannot be null");
        }

        if (departure.isAfter(arrival)) {
            throw new DomainException("Departure time cannot be after arrival time");
        }

        if (departure.isBefore(LocalDateTime.now().minusDays(1))) {
            throw new DomainException("Departure time cannot be in the past");
        }

        Duration flightDuration = Duration.between(departure, arrival);
        if (flightDuration.toHours() > MAX_FLIGHT_DURATION_HOURS) {
            throw new DomainException("Flight duration cannot exceed " + MAX_FLIGHT_DURATION_HOURS + " hours");
        }
    }

    private void validateContactDetails(String contactDetails) {
        if (contactDetails == null || contactDetails.trim().isEmpty()) {
            throw new DomainException("Contact details cannot be null or empty");
        }

        // Extract phone number from contact details and validate
        String[] parts = contactDetails.split(",");
        boolean hasValidPhone = false;
        for (String part : parts) {
            if (part.trim().startsWith("phone:")) {
                String phone = part.substring(6).trim();
                if (PHONE_PATTERN.matcher(phone).matches()) {
                    hasValidPhone = true;
                    break;
                }
            }
        }

        if (!hasValidPhone) {
            throw new DomainException("Contact details must include a valid phone number");
        }
    }
}
