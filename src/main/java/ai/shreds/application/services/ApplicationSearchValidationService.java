package ai.shreds.application.services;

import ai.shreds.shared.dtos.SharedFlightSearchRequest;
import ai.shreds.application.exceptions.ApplicationInvalidSearchException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * Service responsible for validating flight search parameters.
 * Implements comprehensive validation rules for flight searches.
 */
@Service
public class ApplicationSearchValidationService {

    private static final Set<String> VALID_SEAT_CLASSES = Set.of(
            "ECONOMY", "PREMIUM_ECONOMY", "BUSINESS", "FIRST");
    
    private static final int MAX_SEARCH_DAYS_IN_ADVANCE = 365;
    private static final int MIN_CONNECTION_TIME_MINUTES = 30;
    private static final int MAX_CONNECTION_TIME_HOURS = 12;

    /**
     * Validates all search parameters for consistency and business rules.
     *
     * @param request the search request to validate
     * @throws ApplicationInvalidSearchException if any validation fails
     */
    public void validateSearchParameters(SharedFlightSearchRequest request) {
        validateRequiredFields(request);
        validateDates(request);
        validateLocations(request);
        validateSeatClass(request);
    }

    private void validateRequiredFields(SharedFlightSearchRequest request) {
        if (request == null) {
            throw new ApplicationInvalidSearchException("Search request cannot be null", "INVALID_REQUEST");
        }
        if (request.getOrigin() == null || request.getOrigin().isBlank()) {
            throw new ApplicationInvalidSearchException("Origin is required", "MISSING_ORIGIN");
        }
        if (request.getDestination() == null || request.getDestination().isBlank()) {
            throw new ApplicationInvalidSearchException("Destination is required", "MISSING_DESTINATION");
        }
        if (request.getDepartureDate() == null) {
            throw new ApplicationInvalidSearchException("Departure date is required", "MISSING_DEPARTURE_DATE");
        }
    }

    private void validateDates(SharedFlightSearchRequest request) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxFutureDate = now.plusDays(MAX_SEARCH_DAYS_IN_ADVANCE);

        if (request.getDepartureDate().isBefore(now)) {
            throw new ApplicationInvalidSearchException(
                    "Departure date cannot be in the past", 
                    "INVALID_DEPARTURE_DATE");
        }

        if (request.getDepartureDate().isAfter(maxFutureDate)) {
            throw new ApplicationInvalidSearchException(
                    String.format("Cannot search flights more than %d days in advance", MAX_SEARCH_DAYS_IN_ADVANCE),
                    "FUTURE_DATE_TOO_FAR");
        }

        if (request.getReturnDate() != null) {
            if (request.getReturnDate().isBefore(request.getDepartureDate())) {
                throw new ApplicationInvalidSearchException(
                        "Return date must be after departure date",
                        "INVALID_RETURN_DATE");
            }

            long daysBetween = ChronoUnit.DAYS.between(request.getDepartureDate(), request.getReturnDate());
            if (daysBetween > MAX_SEARCH_DAYS_IN_ADVANCE) {
                throw new ApplicationInvalidSearchException(
                        "Trip duration cannot exceed maximum allowed period",
                        "TRIP_DURATION_TOO_LONG");
            }
        }
    }

    private void validateLocations(SharedFlightSearchRequest request) {
        if (request.getOrigin().length() != 3 || !request.getOrigin().matches("^[A-Z]{3}$")) {
            throw new ApplicationInvalidSearchException(
                    "Origin must be a valid 3-letter IATA airport code",
                    "INVALID_ORIGIN_FORMAT");
        }

        if (request.getDestination().length() != 3 || !request.getDestination().matches("^[A-Z]{3}$")) {
            throw new ApplicationInvalidSearchException(
                    "Destination must be a valid 3-letter IATA airport code",
                    "INVALID_DESTINATION_FORMAT");
        }

        if (request.getOrigin().equals(request.getDestination())) {
            throw new ApplicationInvalidSearchException(
                    "Origin and destination cannot be the same",
                    "SAME_ORIGIN_DESTINATION");
        }
    }

    private void validateSeatClass(SharedFlightSearchRequest request) {
        if (request.getSeatClass() != null && !request.getSeatClass().isBlank()) {
            String normalizedSeatClass = request.getSeatClass().toUpperCase();
            if (!VALID_SEAT_CLASSES.contains(normalizedSeatClass)) {
                throw new ApplicationInvalidSearchException(
                        "Invalid seat class. Must be one of: " + String.join(", ", VALID_SEAT_CLASSES),
                        "INVALID_SEAT_CLASS");
            }
        }
    }
}
