package ai.shreds.domain.entities;

import ai.shreds.shared.dtos.SharedSeatAvailabilityDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

/**
 * Domain entity representing seat availability for a specific flight and class.
 * Contains business logic for seat management and validation.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEntitySeatAvailability {

    private UUID id;
    private UUID flightId;
    private String seatClass;
    private int availableSeats;

    /**
     * Updates the number of available seats.
     * @param newCount the new seat count
     * @throws IllegalArgumentException if count is negative
     */
    public void updateAvailableSeats(int newCount) {
        if (newCount < 0) {
            throw new IllegalArgumentException("Available seats cannot be negative");
        }
        this.availableSeats = newCount;
    }

    /**
     * Decrements available seats by the specified amount.
     * @param count number of seats to remove
     * @return true if seats were successfully decremented
     * @throws IllegalArgumentException if count is negative or greater than available seats
     */
    public boolean decrementSeats(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Cannot decrement by negative number");
        }
        if (count > availableSeats) {
            return false;
        }
        this.availableSeats -= count;
        return true;
    }

    /**
     * Increments available seats by the specified amount.
     * @param count number of seats to add
     * @throws IllegalArgumentException if count is negative
     */
    public void incrementSeats(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Cannot increment by negative number");
        }
        this.availableSeats += count;
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
     * Validates the entity state.
     * @throws IllegalStateException if the entity is in an invalid state
     */
    public void validate() {
        if (flightId == null) {
            throw new IllegalStateException("Flight ID is required");
        }
        if (seatClass == null || seatClass.trim().isEmpty()) {
            throw new IllegalStateException("Seat class is required");
        }
        if (availableSeats < 0) {
            throw new IllegalStateException("Available seats cannot be negative");
        }
    }

    /**
     * Converts this domain entity to a shared DTO.
     * @return SharedSeatAvailabilityDTO representation
     */
    public SharedSeatAvailabilityDTO toSharedSeatAvailabilityDTO() {
        return SharedSeatAvailabilityDTO.builder()
                .seatClass(this.seatClass)
                .availableSeats(this.availableSeats)
                .build();
    }
}
