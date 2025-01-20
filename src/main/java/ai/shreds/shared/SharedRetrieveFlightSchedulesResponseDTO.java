package ai.shreds.shared.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data Transfer Object representing the response for flight schedules retrieval.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SharedRetrieveFlightSchedulesResponseDTO {
    
    /**
     * List of flight schedules matching the search criteria.
     */
    private List<SharedFlightScheduleResponseDTO> flights;

    /**
     * Response message providing additional context.
     */
    private String message;

    /**
     * Total number of flights found.
     */
    private Integer totalFlights;

    /**
     * Number of unique airlines in results.
     */
    private Integer uniqueAirlines;

    /**
     * Search parameters used.
     */
    private SharedRetrieveFlightSchedulesRequestParams searchParams;

    /**
     * Time when the search was performed.
     */
    private LocalDateTime searchTime;

    /**
     * Whether results are from cache.
     */
    private Boolean fromCache;

    /**
     * Cache expiration time if applicable.
     */
    private LocalDateTime cacheExpiration;

    /**
     * Number of direct flights found.
     */
    private Integer directFlights;

    /**
     * Number of connecting flights found.
     */
    private Integer connectingFlights;

    /**
     * Lowest fare found across all flights.
     */
    private Double lowestFare;

    /**
     * Highest fare found across all flights.
     */
    private Double highestFare;

    /**
     * Average fare across all flights.
     */
    private Double averageFare;

    /**
     * Fare distribution by airline.
     */
    private Map<String, FareRange> faresByAirline;

    /**
     * Available seat count by class.
     */
    private Map<String, Integer> seatsByClass;

    /**
     * Any warnings or notices about the results.
     */
    private List<String> notices;

    /**
     * Factory method to create a success response.
     */
    public static SharedRetrieveFlightSchedulesResponseDTO success(
            List<SharedFlightScheduleResponseDTO> flights,
            SharedRetrieveFlightSchedulesRequestParams params) {
        
        Map<String, FareRange> faresByAirline = calculateFaresByAirline(flights);
        Map<String, Integer> seatsByClass = calculateSeatsByClass(flights);
        
        return SharedRetrieveFlightSchedulesResponseDTO.builder()
                .flights(flights)
                .totalFlights(flights.size())
                .uniqueAirlines((int) flights.stream()
                        .map(SharedFlightScheduleResponseDTO::getAirlineId)
                        .distinct()
                        .count())
                .searchParams(params)
                .searchTime(LocalDateTime.now())
                .directFlights((int) flights.stream()
                        .filter(f -> !Boolean.TRUE.equals(f.getIsCodeshare()))
                        .count())
                .connectingFlights((int) flights.stream()
                        .filter(f -> Boolean.TRUE.equals(f.getIsCodeshare()))
                        .count())
                .lowestFare(calculateLowestFare(flights))
                .highestFare(calculateHighestFare(flights))
                .averageFare(calculateAverageFare(flights))
                .faresByAirline(faresByAirline)
                .seatsByClass(seatsByClass)
                .message(formatSuccessMessage(flights))
                .build();
    }

    /**
     * Factory method to create an empty response.
     */
    public static SharedRetrieveFlightSchedulesResponseDTO empty(
            SharedRetrieveFlightSchedulesRequestParams params) {
        return SharedRetrieveFlightSchedulesResponseDTO.builder()
                .flights(List.of())
                .totalFlights(0)
                .uniqueAirlines(0)
                .searchParams(params)
                .searchTime(LocalDateTime.now())
                .message("No flights found matching your criteria")
                .build();
    }

    /**
     * Factory method to create an error response.
     */
    public static SharedRetrieveFlightSchedulesResponseDTO error(String errorMessage) {
        return SharedRetrieveFlightSchedulesResponseDTO.builder()
                .message("Error: " + errorMessage)
                .searchTime(LocalDateTime.now())
                .build();
    }

    /**
     * Gets available airlines with their flight counts.
     */
    public Map<String, Long> getAirlineFlightCounts() {
        if (flights == null) return Map.of();
        return flights.stream()
                .collect(Collectors.groupingBy(
                    SharedFlightScheduleResponseDTO::getAirlineName,
                    Collectors.counting()
                ));
    }

    /**
     * Gets summary statistics for the search results.
     */
    public Map<String, Object> getSearchStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalFlights", totalFlights);
        stats.put("uniqueAirlines", uniqueAirlines);
        stats.put("directFlights", directFlights);
        stats.put("connectingFlights", connectingFlights);
        stats.put("lowestFare", lowestFare);
        stats.put("highestFare", highestFare);
        stats.put("averageFare", averageFare);
        stats.put("searchTime", searchTime);
        return stats;
    }

    @Data
    @Builder
    public static class FareRange {
        private Double lowest;
        private Double highest;
        private Double average;
        private int flightCount;
    }

    private static Map<String, FareRange> calculateFaresByAirline(
            List<SharedFlightScheduleResponseDTO> flights) {
        if (flights == null) return Map.of();

        return flights.stream()
                .collect(Collectors.groupingBy(
                    SharedFlightScheduleResponseDTO::getAirlineName,
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        airlineFlights -> {
                            List<Double> fares = airlineFlights.stream()
                                    .map(SharedFlightScheduleResponseDTO::getLowestFare)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());

                            return FareRange.builder()
                                    .lowest(fares.stream().min(Double::compareTo).orElse(null))
                                    .highest(fares.stream().max(Double::compareTo).orElse(null))
                                    .average(fares.stream().mapToDouble(Double::doubleValue).average().orElse(0.0))
                                    .flightCount(airlineFlights.size())
                                    .build();
                        }
                    )
                ));
    }

    private static Map<String, Integer> calculateSeatsByClass(
            List<SharedFlightScheduleResponseDTO> flights) {
        if (flights == null) return Map.of();

        Map<String, Integer> seats = new HashMap<>();
        flights.forEach(flight -> {
            addToMap(seats, "ECONOMY", flight.getBasicEconomyAvailable());
            addToMap(seats, "BUSINESS", flight.getBusinessClassAvailable());
            addToMap(seats, "FIRST", flight.getFirstClassAvailable());
        });
        return seats;
    }

    private static void addToMap(Map<String, Integer> map, String key, Integer value) {
        if (value != null) {
            map.merge(key, value, Integer::sum);
        }
    }

    private static Double calculateLowestFare(List<SharedFlightScheduleResponseDTO> flights) {
        if (flights == null) return null;
        return flights.stream()
                .map(SharedFlightScheduleResponseDTO::getLowestFare)
                .filter(Objects::nonNull)
                .min(Double::compareTo)
                .orElse(null);
    }

    private static Double calculateHighestFare(List<SharedFlightScheduleResponseDTO> flights) {
        if (flights == null) return null;
        return flights.stream()
                .map(SharedFlightScheduleResponseDTO::getLowestFare)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(null);
    }

    private static Double calculateAverageFare(List<SharedFlightScheduleResponseDTO> flights) {
        if (flights == null) return null;
        return flights.stream()
                .map(SharedFlightScheduleResponseDTO::getLowestFare)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private static String formatSuccessMessage(List<SharedFlightScheduleResponseDTO> flights) {
        if (flights == null || flights.isEmpty()) {
            return "No flights found matching your criteria";
        }

        StringBuilder message = new StringBuilder()
                .append(String.format("Found %d flights", flights.size()));

        long directCount = flights.stream()
                .filter(f -> !Boolean.TRUE.equals(f.getIsCodeshare()))
                .count();

        if (directCount > 0) {
            message.append(String.format(", %d direct", directCount));
        }

        long connectingCount = flights.size() - directCount;
        if (connectingCount > 0) {
            message.append(String.format(", %d connecting", connectingCount));
        }

        return message.toString();
    }
}
