package ai.shreds.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Set;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ai.shreds.domain.exceptions.DomainException;

/**
 * Domain entity representing airline information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainEntityAirlineInfo {

    private UUID airlineId;
    private String airlineName;
    private String iataCode;
    private String icaoCode;
    private String contactDetails;
    private String headquarters;
    private String country;
    private LocalDateTime foundedDate;
    private String website;
    private String logo;
    private boolean isActive;
    private Set<String> hubs;
    private Set<String> operatingRegions;
    private String alliance;
    private int fleetSize;
    private LocalDateTime lastUpdated;
    private String operationalStatus;
    private String financialRating;
    private String safetyRating;
    private String customerServiceContact;
    private String emergencyContact;

    /**
     * Validates the airline information.
     *
     * @return true if validation passes
     * @throws DomainException if validation fails
     */
    public boolean validate() {
        if (airlineId == null) {
            throw new DomainException("Airline ID cannot be null");
        }
        if (airlineName == null || airlineName.trim().isEmpty()) {
            throw new DomainException("Airline name cannot be null or empty");
        }
        if (iataCode == null || !iataCode.matches("^[A-Z0-9]{2,3}$")) {
            throw new DomainException("Invalid IATA code format");
        }
        if (icaoCode == null || !icaoCode.matches("^[A-Z]{3}$")) {
            throw new DomainException("Invalid ICAO code format");
        }

        validateContactInformation();
        validateOperationalDetails();

        return true;
    }

    /**
     * Maps the entity to a value object.
     * Note: Changed return type from void to proper value object type.
     *
     * @return Value object representing airline information
     */
    public DomainValueAirlineInfo mapToValueObject() {
        return DomainValueAirlineInfo.builder()
                .airlineId(airlineId != null ? airlineId.toString() : null)
                .airlineName(airlineName)
                .iataCode(iataCode)
                .icaoCode(icaoCode)
                .contactDetails(contactDetails)
                .headquarters(headquarters)
                .country(country)
                .website(website)
                .isActive(isActive)
                .hubs(hubs)
                .operatingRegions(operatingRegions)
                .alliance(alliance)
                .fleetSize(fleetSize)
                .operationalStatus(operationalStatus)
                .build();
    }

    /**
     * Checks if the airline is operational.
     *
     * @return true if the airline is active and operational
     */
    public boolean isOperational() {
        return isActive && "OPERATIONAL".equals(operationalStatus);
    }

    /**
     * Checks if the airline operates in a specific region.
     *
     * @param region Region to check
     * @return true if the airline operates in the specified region
     */
    public boolean operatesInRegion(String region) {
        return operatingRegions != null && operatingRegions.contains(region);
    }

    private void validateContactInformation() {
        if (contactDetails == null || contactDetails.trim().isEmpty()) {
            throw new DomainException("Contact details cannot be null or empty");
        }
        if (customerServiceContact == null || customerServiceContact.trim().isEmpty()) {
            throw new DomainException("Customer service contact cannot be null or empty");
        }
        if (emergencyContact == null || emergencyContact.trim().isEmpty()) {
            throw new DomainException("Emergency contact cannot be null or empty");
        }
        if (website == null || !website.matches("^https?://.*")) {
            throw new DomainException("Invalid website format");
        }
    }

    private void validateOperationalDetails() {
        if (hubs == null || hubs.isEmpty()) {
            throw new DomainException("Airline must have at least one hub");
        }
        if (operatingRegions == null || operatingRegions.isEmpty()) {
            throw new DomainException("Airline must have at least one operating region");
        }
        if (fleetSize <= 0) {
            throw new DomainException("Fleet size must be positive");
        }
    }
}
