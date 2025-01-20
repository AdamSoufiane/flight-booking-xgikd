package ai.shreds.domain.value_objects;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;

/**
 * Value object representing airline information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainValueAirlineInfo {
    private String airlineId;
    private String airlineName;
    private String iataCode;
    private String icaoCode;
    private String contactDetails;
    private String headquarters;
    private String country;
    private String website;
    private boolean isActive;
    private Set<String> hubs;
    private Set<String> operatingRegions;
    private String alliance;
    private int fleetSize;
    private String operationalStatus;
}
