package ai.shreds.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ai.shreds.domain.value_objects.DomainValueSeatAvailability;
import ai.shreds.domain.exceptions.DomainException;

/**
 * Domain entity representing seat availability for a flight.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEntitySeatAvailability {

    private UUID id;
    private UUID flightId;
    private String seatClass;
    private Integer totalSeats;
    private Integer availableSeats;
    private Double basePrice;
    private Double currentPrice;
    private LocalDateTime lastUpdated;
    private String fareCode;
    private boolean isWaitlistAvailable;
    private Integer waitlistCapacity;
    private Integer currentWaitlistCount;
    private String restrictions;
    private boolean isRefundable;
    private Double cancellationFee;
    private Integer minimumStayDays;
    private Integer maximumStayDays;

    /**
     * Validates the seat availability data.
     *
     * @return true if validation passes
     * @throws DomainException if validation fails
     */
    public boolean validateSeatCount() {
        if (id == null) {
            throw new DomainException("Seat availability ID cannot be null");
        }
        if (flightId == null) {
            throw new DomainException("Flight ID cannot be null");
        }
        if (seatClass == null || seatClass.trim().isEmpty()) {
            throw new DomainException("Seat class cannot be null or empty");
        }
        if (totalSeats == null || totalSeats < 0) {
            throw new DomainException("Total seats must be non-negative");
        }
        if (availableSeats == null || availableSeats < 0) {
            throw new DomainException("Available seats must be non-negative");
        }
        if (availableSeats > totalSeats) {
            throw new DomainException("Available seats cannot exceed total seats");
        }

        validatePricing();
        validateWaitlist();
        validateStayRestrictions();

        return true;
    }

    /**
     * Maps the entity to a value object.
     *
     * @return DomainValueSeatAvailability representing this entity
     */
    public DomainValueSeatAvailability mapToValueObject() {
        return DomainValueSeatAvailability.builder()
                .flightId(flightId != null ? flightId.toString() : null)
                .seatClass(seatClass)
                .totalSeats(totalSeats != null ? totalSeats : 0)
                .availableSeats(availableSeats != null ? availableSeats : 0)
                .basePrice(basePrice)
                .currentPrice(currentPrice)
                .fareCode(fareCode)
                .isWaitlistAvailable(isWaitlistAvailable)
                .isRefundable(isRefundable)
                .build();
    }

    /**
     * Checks if seats are available for booking.
     *
     * @param requestedSeats number of seats requested
     * @return true if seats are available
     */
    public boolean hasAvailableSeats(int requestedSeats) {
        return availableSeats != null && availableSeats >= requestedSeats;
    }

    /**
     * Calculates the total price for requested seats.
     *
     * @param numberOfSeats number of seats to calculate price for
     * @return total price
     */
    public double calculateTotalPrice(int numberOfSeats) {
        if (currentPrice == null || numberOfSeats <= 0) {
            throw new DomainException("Invalid price calculation parameters");
        }
        return currentPrice * numberOfSeats;
    }

    /**
     * Updates the available seat count.
     *
     * @param count number of seats to add (positive) or remove (negative)
     */
    public void updateAvailableSeats(int count) {
        if (availableSeats == null) {
            throw new DomainException("Available seats not initialized");
        }

        int newCount = availableSeats + count;
        if (newCount < 0 || newCount > totalSeats) {
            throw new DomainException("Invalid seat count update");
        }

        availableSeats = newCount;
        lastUpdated = LocalDateTime.now();
    }

    private void validatePricing() {
        if (basePrice == null || basePrice < 0) {
            throw new DomainException("Base price must be non-negative");
        }
        if (currentPrice == null || currentPrice < 0) {
            throw new DomainException("Current price must be non-negative");
        }
        if (fareCode == null || fareCode.trim().isEmpty()) {
            throw new DomainException("Fare code cannot be null or empty");
        }
    }

    private void validateWaitlist() {
        if (isWaitlistAvailable) {
            if (waitlistCapacity == null || waitlistCapacity < 0) {
                throw new DomainException("Waitlist capacity must be non-negative when waitlist is available");
            }
            if (currentWaitlistCount == null || currentWaitlistCount < 0) {
                throw new DomainException("Current waitlist count must be non-negative");
            }
            if (currentWaitlistCount > waitlistCapacity) {
                throw new DomainException("Current waitlist count cannot exceed capacity");
            }
        }
    }

    private void validateStayRestrictions() {
        if (minimumStayDays != null && minimumStayDays < 0) {
            throw new DomainException("Minimum stay days must be non-negative");
        }
        if (maximumStayDays != null && maximumStayDays < 0) {
            throw new DomainException("Maximum stay days must be non-negative");
        }
        if (minimumStayDays != null && maximumStayDays != null && 
            minimumStayDays > maximumStayDays) {
            throw new DomainException("Minimum stay cannot exceed maximum stay");
        }
    }
}
