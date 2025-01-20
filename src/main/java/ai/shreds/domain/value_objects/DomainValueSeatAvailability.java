package ai.shreds.domain.value_objects;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Value object representing seat availability information for a flight.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainValueSeatAvailability {
    private String flightId;
    private String seatClass;
    private int totalSeats;
    private int availableSeats;
    private Double basePrice;
    private Double currentPrice;
    private String fareCode;
    private boolean isWaitlistAvailable;
    private boolean isRefundable;
    private Double cancellationFee;
    private String fareConditions;
    private String baggageAllowance;
    private String mealOptions;
    private Integer minimumStayDays;
    private Integer maximumStayDays;
    private boolean isUpgradeable;
    private String loyaltyEarning;
    
    /**
     * Calculates the occupancy percentage.
     *
     * @return Percentage of seats occupied
     */
    public double getOccupancyPercentage() {
        if (totalSeats == 0) return 0.0;
        return ((totalSeats - availableSeats) * 100.0) / totalSeats;
    }

    /**
     * Checks if seats are available.
     *
     * @param requestedSeats Number of seats needed
     * @return true if enough seats are available
     */
    public boolean hasAvailability(int requestedSeats) {
        return availableSeats >= requestedSeats;
    }

    /**
     * Calculates total price for requested seats.
     *
     * @param numberOfSeats Number of seats to calculate price for
     * @return Total price for requested seats
     */
    public Double calculateTotalPrice(int numberOfSeats) {
        if (currentPrice == null || numberOfSeats <= 0) return null;
        return currentPrice * numberOfSeats;
    }

    /**
     * Gets a formatted string of fare conditions.
     *
     * @return Formatted string with key fare information
     */
    public String getFormattedFareInfo() {
        StringBuilder info = new StringBuilder()
            .append(seatClass)
            .append(" (")
            .append(fareCode)
            .append(") - ")
            .append(String.format("$%.2f", currentPrice));

        if (isRefundable) {
            info.append(" [Refundable]")
                .append(String.format(" (Fee: $%.2f)", cancellationFee));
        } else {
            info.append(" [Non-refundable]")
        }

        return info.toString();
    }
}
