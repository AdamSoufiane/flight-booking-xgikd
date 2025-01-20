package ai.shreds.domain.value_objects;

import lombok.Value;
import lombok.Builder;

/**
 * Value object representing seat availability information.
 * Immutable and thread-safe with built-in validation.
 */
@Value
@Builder
public class DomainValueSeatAvailability {

    String seatClass;
    int availableSeats;

    /**
     * Custom builder implementation to add validation.
     */
    public static class DomainValueSeatAvailabilityBuilder {
        /**
         * Builds the value object with validation.
         * @return validated DomainValueSeatAvailability
         * @throws IllegalArgumentException if validation fails
         */
        public DomainValueSeatAvailability build() {
            validate();
            return new DomainValueSeatAvailability(seatClass, availableSeats);
        }

        private void validate() {
            if (seatClass == null || seatClass.trim().isEmpty()) {
                throw new IllegalArgumentException("Seat class is required");
            }
            if (availableSeats < 0) {
                throw new IllegalArgumentException("Available seats cannot be negative");
            }
        }
    }

    /**
     * Checks if seats are available.
     * @return true if there are available seats
     */
    public boolean hasAvailability() {
        return availableSeats > 0;
    }

    /**
     * Checks if the specified number of seats are available.
     * @param requestedSeats number of seats needed
     * @return true if enough seats are available
     */
    public boolean hasAvailability(int requestedSeats) {
        return availableSeats >= requestedSeats;
    }

    /**
     * Creates a new instance with updated seat count.
     * @param newCount the new number of available seats
     * @return new DomainValueSeatAvailability instance
     * @throws IllegalArgumentException if newCount is negative
     */
    public DomainValueSeatAvailability withUpdatedSeatCount(int newCount) {
        return DomainValueSeatAvailability.builder()
                .seatClass(this.seatClass)
                .availableSeats(newCount)
                .build();
    }
}
