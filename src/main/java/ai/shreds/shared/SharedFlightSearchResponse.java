package ai.shreds.shared.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * DTO representing the response of a flight search.
 * Includes pagination information and metadata about the search results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedFlightSearchResponse {
    @Valid
    @NotNull(message = "Flight list cannot be null")
    @Builder.Default
    private List<SharedFlightDTO> flightList = new ArrayList<>();

    @NotNull(message = "Total results count is required")
    @Min(value = 0, message = "Total results cannot be negative")
    private Integer totalResults;
    
    @NotNull(message = "Page number is required")
    @Min(value = 0, message = "Page number cannot be negative")
    @Builder.Default
    private Integer page = 0;
    
    @NotNull(message = "Page size is required")
    @Min(value = 1, message = "Page size must be at least 1")
    @Builder.Default
    private Integer pageSize = 20;
    
    @NotNull(message = "Search timestamp is required")
    private LocalDateTime searchTimestamp;
    
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Checks if the search returned any results.
     * @return true if there are flights in the result, false otherwise
     */
    public boolean hasResults() {
        return flightList != null && !flightList.isEmpty();
    }

    /**
     * Gets the total number of pages based on page size.
     * @return total number of pages
     */
    public int getTotalPages() {
        if (totalResults == null || totalResults == 0) return 0;
        return (int) Math.ceil((double) totalResults / pageSize);
    }

    /**
     * Gets all available flights for a specific seat class.
     * @param seatClass the class to filter by
     * @return list of flights with available seats in the specified class
     */
    public List<SharedFlightDTO> getAvailableFlightsForClass(String seatClass) {
        return flightList.stream()
                .filter(flight -> flight.hasAvailabilityForClass(seatClass))
                .collect(Collectors.toList());
    }

    /**
     * Gets flights within a specific price range.
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @return list of flights within the price range
     */
    public List<SharedFlightDTO> getFlightsInPriceRange(double minPrice, double maxPrice) {
        return flightList.stream()
                .filter(flight -> {
                    Double price = (Double) flight.getMetadata().get("price");
                    return price != null && price >= minPrice && price <= maxPrice;
                })
                .collect(Collectors.toList());
    }

    /**
     * Gets flights with duration less than specified hours.
     * @param maxHours maximum flight duration in hours
     * @return list of flights within duration limit
     */
    public List<SharedFlightDTO> getFlightsWithinDuration(int maxHours) {
        return flightList.stream()
                .filter(flight -> flight.getFlightDuration().compareTo(Duration.ofHours(maxHours)) <= 0)
                .collect(Collectors.toList());
    }

    /**
     * Adds metadata to the search response.
     * @param key metadata key
     * @param value metadata value
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    /**
     * Creates a subset of the response for pagination.
     * @param pageNumber the page number to create
     * @param size the size of each page
     * @return new response object with paginated results
     * @throws IllegalArgumentException if pagination parameters are invalid
     */
    public SharedFlightSearchResponse createPaginatedResponse(int pageNumber, int size) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must be at least 1");
        }

        int fromIndex = pageNumber * size;
        int toIndex = Math.min(fromIndex + size, flightList.size());

        if (fromIndex >= flightList.size()) {
            return SharedFlightSearchResponse.builder()
                    .flightList(new ArrayList<>())
                    .totalResults(totalResults)
                    .page(pageNumber)
                    .pageSize(size)
                    .searchTimestamp(searchTimestamp)
                    .metadata(new HashMap<>(metadata))
                    .build();
        }

        return SharedFlightSearchResponse.builder()
                .flightList(new ArrayList<>(flightList.subList(fromIndex, toIndex)))
                .totalResults(totalResults)
                .page(pageNumber)
                .pageSize(size)
                .searchTimestamp(searchTimestamp)
                .metadata(new HashMap<>(metadata))
                .build();
    }

    /**
     * Validates the response data.
     * @throws IllegalStateException if validation fails
     */
    public void validate() {
        if (flightList == null) {
            throw new IllegalStateException("Flight list cannot be null");
        }
        if (totalResults == null || totalResults < 0) {
            throw new IllegalStateException("Invalid total results count");
        }
        if (page == null || page < 0) {
            throw new IllegalStateException("Invalid page number");
        }
        if (pageSize == null || pageSize < 1) {
            throw new IllegalStateException("Invalid page size");
        }
        if (searchTimestamp == null) {
            throw new IllegalStateException("Search timestamp is required");
        }
        flightList.forEach(SharedFlightDTO::validate);
    }
}
