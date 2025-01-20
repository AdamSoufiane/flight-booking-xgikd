package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainEntityFlightSchedule;
import ai.shreds.domain.entities.DomainEntitySeatAvailability;
import ai.shreds.domain.exceptions.DomainException;
import ai.shreds.domain.ports.DomainPortFlightRepository;
import ai.shreds.domain.ports.DomainPortSeatAvailabilityRepository;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;

/**
 * Domain service handling core flight operations and business rules.
 */
public class DomainFlightService {

    private static final int MIN_TURNAROUND_TIME_MINUTES = 45;
    private static final int MAX_DAILY_FLIGHTS_PER_AIRCRAFT = 8;
    
    private final DomainPortFlightRepository flightRepository;
    private final DomainPortSeatAvailabilityRepository seatAvailabilityRepository;
    private final DomainDataValidationService validationService;

    public DomainFlightService(
            DomainPortFlightRepository flightRepository,
            DomainPortSeatAvailabilityRepository seatAvailabilityRepository,
            DomainDataValidationService validationService) {
        this.flightRepository = flightRepository;
        this.seatAvailabilityRepository = seatAvailabilityRepository;
        this.validationService = validationService;
    }

    /**
     * Performs flight operations including validation and business rules.
     *
     * @param flight The flight schedule to process
     * @throws DomainException if operations cannot be performed
     */
    public void performFlightOperations(DomainEntityFlightSchedule flight) {
        if (flight == null) {
            throw new DomainException("Flight schedule cannot be null");
        }

        // Validate the flight schedule
        if (!validationService.validateFlightSchedule(flight)) {
            throw new DomainException("Flight schedule validation failed");
        }

        // Apply business rules
        applyBusinessRules(flight);

        // Save the flight schedule
        flightRepository.save(flight);
    }

    /**
     * Cancels a flight and handles related operations.
     *
     * @param flightId ID of the flight to cancel
     * @param reason Reason for cancellation
     * @throws DomainException if cancellation cannot be performed
     */
    public void cancelFlight(String flightId, String reason) {
        DomainEntityFlightSchedule flight = flightRepository.findFlightScheduleById(flightId);
        if (flight == null) {
            throw new DomainException("Flight not found: " + flightId);
        }

        // Check if flight can be cancelled
        if (flight.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new DomainException("Cannot cancel a flight that has already departed");
        }

        // Update flight status
        flight.setStatus("CANCELLED");
        flight.setCancellationReason(reason);
        flight.setLastUpdated(LocalDateTime.now());

        // Save the updated flight
        flightRepository.save(flight);
    }

    /**
     * Updates seat availability for a flight.
     *
     * @param flightId ID of the flight
     * @param seatClass Class of seats to update
     * @param count Number of seats to add or remove (positive or negative)
     * @throws DomainException if update cannot be performed
     */
    public void updateSeatAvailability(String flightId, String seatClass, int count) {
        List<DomainEntitySeatAvailability> seatAvailability = 
                seatAvailabilityRepository.findSeatAvailabilityByFlightId(flightId);

        DomainEntitySeatAvailability seats = seatAvailability.stream()
                .filter(s -> s.getSeatClass().equals(seatClass))
                .findFirst()
                .orElseThrow(() -> new DomainException(
                    "Seat class " + seatClass + " not found for flight " + flightId));

        int newAvailable = seats.getAvailableSeats() + count;
        if (newAvailable < 0 || newAvailable > seats.getTotalSeats()) {
            throw new DomainException("Invalid seat count adjustment");
        }

        seats.setAvailableSeats(newAvailable);
        seats.setLastUpdated(LocalDateTime.now());
        seatAvailabilityRepository.save(seats);
    }

    private void applyBusinessRules(DomainEntityFlightSchedule flight) {
        validateTurnaroundTime(flight);
        validateDailyFlightLimit(flight);
        validateRouteRestrictions(flight);
    }

    private void validateTurnaroundTime(DomainEntityFlightSchedule newFlight) {
        List<DomainEntityFlightSchedule> aircraftFlights = 
                flightRepository.findByAircraftId(newFlight.getAircraftId());

        for (DomainEntityFlightSchedule existingFlight : aircraftFlights) {
            if (existingFlight.getFlightId().equals(newFlight.getFlightId())) {
                continue; // Skip the current flight
            }

            // Check if there's enough turnaround time between flights
            Duration timeBetweenFlights = Duration.between(
                    existingFlight.getArrivalTime(),
                    newFlight.getDepartureTime());

            if (Math.abs(timeBetweenFlights.toMinutes()) < MIN_TURNAROUND_TIME_MINUTES) {
                throw new DomainException("Insufficient turnaround time between flights");
            }
        }
    }

    private void validateDailyFlightLimit(DomainEntityFlightSchedule flight) {
        LocalDateTime dayStart = flight.getDepartureTime().toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        long dailyFlights = flightRepository.findByAircraftId(flight.getAircraftId())
                .stream()
                .filter(f -> f.getDepartureTime().isAfter(dayStart) && 
                            f.getDepartureTime().isBefore(dayEnd))
                .count();

        if (dailyFlights >= MAX_DAILY_FLIGHTS_PER_AIRCRAFT) {
            throw new DomainException("Maximum daily flight limit exceeded for aircraft");
        }
    }

    private void validateRouteRestrictions(DomainEntityFlightSchedule flight) {
        // Example: Check if the airline is authorized for this route
        if (!isAirlineAuthorizedForRoute(flight.getAirlineId(), 
                flight.getOrigin(), flight.getDestination())) {
            throw new DomainException("Airline not authorized for this route");
        }

        // Example: Check if airports are open during scheduled times
        if (!areAirportsOpenForSchedule(flight)) {
            throw new DomainException("Airports not open during scheduled times");
        }
    }

    private boolean isAirlineAuthorizedForRoute(String airlineId, String origin, String destination) {
        // Implementation would check airline route authorities
        // For now, return true as placeholder
        return true;
    }

    private boolean areAirportsOpenForSchedule(DomainEntityFlightSchedule flight) {
        // Implementation would check airport operating hours
        // For now, return true as placeholder
        return true;
    }
}
