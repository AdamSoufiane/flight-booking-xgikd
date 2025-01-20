package ai.shreds.domain.value_objects;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Value object representing flight search criteria.
 * Immutable and thread-safe with built-in validation.
 */
@Value
@Builder
public class DomainValueFlightSearchCriteria {

    private static final int MAX_SEARCH_DAYS_IN_ADVANCE = 365;

    String origin;
    String destination;
    LocalDateTime departureDate;
    LocalDateTime returnDate;
    String seatClass;

    /**
     * Custom builder implementation to add validation.
     */
    public static class DomainValueFlightSearchCriteriaBuilder {
        /**
         * Builds the value object with validation.
         * @return validated DomainValueFlightSearchCriteria
         * @throws IllegalArgumentException if validation fails
         */
        public DomainValueFlightSearchCriteria build() {
            validate();
            return new DomainValueFlightSearchCriteria(origin, destination, departureDate, returnDate, seatClass);
        }

        private void validate() {
            validateRequiredFields();
            validateDates();
            validateLocations();
        }

        private void validateRequiredFields() {
            if (origin == null || origin.trim().isEmpty()) {
                throw new IllegalArgumentException("Origin is required");
            }
            if (destination == null || destination.trim().isEmpty()) {
                throw new IllegalArgumentException("Destination is required");
            }
            if (departureDate == null) {
                throw new IllegalArgumentException("Departure date is required");
            }
        }

        private void validateDates() {
            LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
            LocalDateTime maxFutureDate = now.plusDays(MAX_SEARCH_DAYS_IN_ADVANCE);

            if (departureDate.isBefore(now)) {
                throw new IllegalArgumentException("Departure date cannot be in the past");
            }

            if (departureDate.isAfter(maxFutureDate)) {
                throw new IllegalArgumentException(
                        String.format("Cannot search flights more than %d days in advance", MAX_SEARCH_DAYS_IN_ADVANCE));
            }

            if (returnDate != null) {
                if (returnDate.isBefore(departureDate)) {
                    throw new IllegalArgumentException("Return date must be after departure date");
                }

                if (returnDate.isAfter(maxFutureDate)) {
                    throw new IllegalArgumentException(
                            String.format("Return date cannot be more than %d days in the future", MAX_SEARCH_DAYS_IN_ADVANCE));
                }
            }
        }

        private void validateLocations() {
            if (!origin.matches("^[A-Z]{3}$")) {
                throw new IllegalArgumentException("Origin must be a valid 3-letter IATA airport code");
            }

            if (!destination.matches("^[A-Z]{3}$")) {
                throw new IllegalArgumentException("Destination must be a valid 3-letter IATA airport code");
            }

            if (origin.equals(destination)) {
                throw new IllegalArgumentException("Origin and destination cannot be the same");
            }
        }
    }

    /**
     * Checks if this is a round trip search.
     * @return true if return date is specified
     */
    public boolean isRoundTrip() {
        return returnDate != null;
    }

    /**
     * Creates a cache key for this search criteria.
     * @return string that can be used as a cache key
     */
    public String toCacheKey() {
        return String.format("%s-%s-%s-%s-%s",
                origin,
                destination,
                departureDate.truncatedTo(ChronoUnit.DAYS),
                returnDate != null ? returnDate.truncatedTo(ChronoUnit.DAYS) : "null",
                Objects.toString(seatClass, "all"));
    }

    /**
     * Creates a new criteria with updated dates.
     * @param newDepartureDate new departure date
     * @param newReturnDate new return date
     * @return new DomainValueFlightSearchCriteria instance
     */
    public DomainValueFlightSearchCriteria withUpdatedDates(LocalDateTime newDepartureDate, LocalDateTime newReturnDate) {
        return DomainValueFlightSearchCriteria.builder()
                .origin(this.origin)
                .destination(this.destination)
                .departureDate(newDepartureDate)
                .returnDate(newReturnDate)
                .seatClass(this.seatClass)
                .build();
    }
}
