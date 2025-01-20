package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityAirlineInfo;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Port interface for airline information repository operations in the domain layer.
 */
public interface DomainPortAirlineInfoRepository {

    /**
     * Creates a new airline information record.
     *
     * @param entity Airline information to create
     * @throws ai.shreds.domain.exceptions.DomainException if creation fails
     */
    void createAirlineInfo(DomainEntityAirlineInfo entity);

    /**
     * Updates an existing airline information record.
     *
     * @param entity Airline information to update
     * @throws ai.shreds.domain.exceptions.DomainException if update fails
     */
    void updateAirlineInfo(DomainEntityAirlineInfo entity);

    /**
     * Finds airline information by string ID.
     *
     * @param airlineId Airline ID
     * @return Found airline information or null
     */
    DomainEntityAirlineInfo findAirlineInfoById(String airlineId);

    /**
     * Saves airline information (creates or updates).
     *
     * @param airlineInfo Airline information to save
     * @return Saved airline information
     */
    DomainEntityAirlineInfo save(DomainEntityAirlineInfo airlineInfo);

    /**
     * Finds airline information by UUID.
     *
     * @param id Airline UUID
     * @return Found airline information or null
     */
    DomainEntityAirlineInfo findById(UUID id);

    /**
     * Finds airlines by country.
     *
     * @param country Country name
     * @return List of airlines in the country
     */
    List<DomainEntityAirlineInfo> findByCountry(String country);

    /**
     * Finds airlines by alliance membership.
     *
     * @param alliance Alliance name
     * @return List of airlines in the alliance
     */
    List<DomainEntityAirlineInfo> findByAlliance(String alliance);

    /**
     * Finds airlines operating in a specific region.
     *
     * @param region Region name
     * @return List of airlines operating in the region
     */
    List<DomainEntityAirlineInfo> findByOperatingRegion(String region);

    /**
     * Finds active airlines with minimum fleet size.
     *
     * @param minFleetSize Minimum fleet size
     * @return List of matching airlines
     */
    List<DomainEntityAirlineInfo> findActiveAirlinesWithMinFleetSize(int minFleetSize);

    /**
     * Finds airlines by operational status.
     *
     * @param status Operational status
     * @return List of airlines with the specified status
     */
    List<DomainEntityAirlineInfo> findByOperationalStatus(String status);

    /**
     * Finds airlines updated after a specific time.
     *
     * @param timestamp Time threshold
     * @return List of recently updated airlines
     */
    List<DomainEntityAirlineInfo> findByLastUpdatedAfter(LocalDateTime timestamp);

    /**
     * Finds airlines by multiple IDs.
     *
     * @param airlineIds List of airline IDs
     * @return List of found airlines
     */
    List<DomainEntityAirlineInfo> findByIds(List<UUID> airlineIds);

    /**
     * Searches airlines by name pattern.
     *
     * @param namePattern Name pattern to match
     * @return List of matching airlines
     */
    List<DomainEntityAirlineInfo> searchByName(String namePattern);

    /**
     * Finds airlines by hub airport.
     *
     * @param hubCode Airport code
     * @return List of airlines with the specified hub
     */
    List<DomainEntityAirlineInfo> findByHub(String hubCode);

    /**
     * Checks if an airline exists.
     *
     * @param airlineId Airline ID
     * @return true if airline exists
     */
    boolean existsById(UUID airlineId);

    /**
     * Deletes an airline record.
     *
     * @param airlineId Airline ID
     */
    void deleteById(UUID airlineId);

    /**
     * Counts total active airlines.
     *
     * @return Number of active airlines
     */
    long countActiveAirlines();
}
