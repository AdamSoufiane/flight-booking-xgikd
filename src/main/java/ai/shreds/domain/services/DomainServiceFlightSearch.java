package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainEntityFlightSchedule;
import ai.shreds.domain.exceptions.DomainException;
import ai.shreds.domain.ports.DomainPortFlightRepository;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Domain service responsible for flight search operations.
 * Implements business logic for searching direct and connecting flights.
 */
public class DomainServiceFlightSearch {

    private static final int MIN_CONNECTION_TIME_MINUTES = 45;
    private static final int MAX_CONNECTION_TIME_MINUTES = 240;
    private final DomainPortFlightRepository flightRepository;

    public DomainServiceFlightSearch(DomainPortFlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    /**
     * Searches for direct flights between origin and destination.
     *
     * @param origin IATA/ICAO code of the origin airport
     * @param destination IATA/ICAO code of the destination airport
     * @return List of matching flight schedules
     * @throws DomainException if validation fails or search operation encounters an error
     */
    public List<DomainEntityFlightSchedule> searchFlights(String origin, String destination) {
        validateSearchParameters(origin, destination);
        return flightRepository.findByOriginAndDestination(origin, destination);
    }

    /**
     * Finds possible connecting flights between origin and destination.
     *
     * @param origin IATA/ICAO code of the origin airport
     * @param destination IATA/ICAO code of the destination airport
     * @param maxConnections Maximum number of connections (1 or 2)
     * @return List of lists, where each inner list represents a connection combination
     * @throws DomainException if validation fails or search operation encounters an error
     */
    public List<List<DomainEntityFlightSchedule>> findConnectingFlights(
            String origin, String destination, int maxConnections) {
        
        validateSearchParameters(origin, destination);
        if (maxConnections < 1 || maxConnections > 2) {
            throw new DomainException("Maximum connections must be either 1 or 2");
        }

        List<List<DomainEntityFlightSchedule>> results = new ArrayList<>();
        
        // Find all flights from origin
        List<DomainEntityFlightSchedule> firstLegFlights = 
                flightRepository.findByOriginAndDestination(origin, null);

        for (DomainEntityFlightSchedule firstLeg : firstLegFlights) {
            if (maxConnections == 1) {
                // Search for direct connections to destination
                findSingleConnections(firstLeg, destination, results);
            } else {
                // Search for double connections
                findDoubleConnections(firstLeg, destination, results);
            }
        }

        return results;
    }

    private void findSingleConnections(
            DomainEntityFlightSchedule firstLeg,
            String finalDestination,
            List<List<DomainEntityFlightSchedule>> results) {
        
        List<DomainEntityFlightSchedule> connectingFlights =
                flightRepository.findByOriginAndDestination(firstLeg.getDestination(), finalDestination);

        for (DomainEntityFlightSchedule connection : connectingFlights) {
            if (isValidConnection(firstLeg, connection)) {
                results.add(List.of(firstLeg, connection));
            }
        }
    }

    private void findDoubleConnections(
            DomainEntityFlightSchedule firstLeg,
            String finalDestination,
            List<List<DomainEntityFlightSchedule>> results) {
        
        // Find all possible second legs
        List<DomainEntityFlightSchedule> secondLegFlights =
                flightRepository.findByOriginAndDestination(firstLeg.getDestination(), null);

        for (DomainEntityFlightSchedule secondLeg : secondLegFlights) {
            if (!isValidConnection(firstLeg, secondLeg)) continue;

            // Find flights from second leg to final destination
            List<DomainEntityFlightSchedule> finalLegFlights =
                    flightRepository.findByOriginAndDestination(secondLeg.getDestination(), finalDestination);

            for (DomainEntityFlightSchedule finalLeg : finalLegFlights) {
                if (isValidConnection(secondLeg, finalLeg)) {
                    results.add(List.of(firstLeg, secondLeg, finalLeg));
                }
            }
        }
    }

    private boolean isValidConnection(DomainEntityFlightSchedule first, DomainEntityFlightSchedule second) {
        // Check if airports match
        if (!first.getDestination().equals(second.getOrigin())) {
            return false;
        }

        // Calculate connection time
        Duration connectionTime = Duration.between(first.getArrivalTime(), second.getDepartureTime());
        long connectionMinutes = connectionTime.toMinutes();

        // Check if connection time is within acceptable range
        return connectionMinutes >= MIN_CONNECTION_TIME_MINUTES && 
               connectionMinutes <= MAX_CONNECTION_TIME_MINUTES;
    }

    private void validateSearchParameters(String origin, String destination) {
        if (origin == null || origin.trim().isEmpty()) {
            throw new DomainException("Origin airport code cannot be null or empty");
        }
        if (destination == null || destination.trim().isEmpty()) {
            throw new DomainException("Destination airport code cannot be null or empty");
        }
        if (origin.equals(destination)) {
            throw new DomainException("Origin and destination airports cannot be the same");
        }

        // Validate airport code format (IATA: 3 letters, ICAO: 4 letters)
        String airportCodePattern = "^[A-Z]{3,4}$";
        if (!origin.matches(airportCodePattern) || !destination.matches(airportCodePattern)) {
            throw new DomainException("Invalid airport code format");
        }
    }
}
