package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.entities.DomainEntityAirlineInfo;
import ai.shreds.domain.entities.DomainEntityFlightSchedule;
import ai.shreds.domain.ports.DomainPortFlightDataSync;
import ai.shreds.domain.value_objects.DomainValueFlightSchedule;
import ai.shreds.domain.value_objects.DomainValueSeatAvailability;
import ai.shreds.infrastructure.exceptions.InfrastructureException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the flight data synchronization port.
 * Handles external API calls to fetch flight-related data.
 */
@Service
public class InfrastructureFlightDataSyncImpl implements DomainPortFlightDataSync {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;

    public InfrastructureFlightDataSyncImpl(
            RestTemplate restTemplate,
            @Value("${external.api.key}") String apiKey,
            @Value("${external.api.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Cacheable(value = "externalFlightData", key = "#root.method.name")
    public List<DomainValueFlightSchedule> fetchExternalFlightData() {
        try {
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            String url = baseUrl + "/flights/schedules";

            // In a real implementation, make the API call
            // ResponseEntity<FlightDataResponse> response = 
            //     restTemplate.exchange(url, HttpMethod.GET, entity, FlightDataResponse.class);

            // For demo, generate mock data
            return generateMockFlightSchedules();
        } catch (Exception e) {
            throw new InfrastructureException("Error fetching external flight data", e);
        }
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Cacheable(value = "externalSeatData", key = "#root.method.name")
    public List<DomainValueSeatAvailability> fetchExternalSeatData() {
        try {
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            String url = baseUrl + "/flights/seats";

            // In a real implementation, make the API call
            // ResponseEntity<SeatDataResponse> response = 
            //     restTemplate.exchange(url, HttpMethod.GET, entity, SeatDataResponse.class);

            // For demo, generate mock data
            return generateMockSeatAvailability();
        } catch (Exception e) {
            throw new InfrastructureException("Error fetching external seat data", e);
        }
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Cacheable(value = "externalAirlineData", key = "#root.method.name")
    public List<DomainEntityAirlineInfo> fetchExternalAirlineData() {
        try {
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            String url = baseUrl + "/airlines";

            // In a real implementation, make the API call
            // ResponseEntity<AirlineDataResponse> response = 
            //     restTemplate.exchange(url, HttpMethod.GET, entity, AirlineDataResponse.class);

            // For demo, generate mock data
            return generateMockAirlineInfo();
        } catch (Exception e) {
            throw new InfrastructureException("Error fetching external airline data", e);
        }
    }

    @Override
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    @Cacheable(value = "externalFlightData", key = "#airlineIds")
    public List<DomainEntityFlightSchedule> fetchFlightData(List<UUID> airlineIds) {
        try {
            HttpEntity<String> entity = new HttpEntity<>(createHeaders());
            String url = baseUrl + "/flights?airlines=" + 
                    airlineIds.stream().map(UUID::toString).collect(Collectors.joining(","));

            // In a real implementation, make the API call
            // ResponseEntity<FlightScheduleResponse> response = 
            //     restTemplate.exchange(url, HttpMethod.GET, entity, FlightScheduleResponse.class);

            // For demo, generate mock data
            return generateMockFlightSchedules(airlineIds);
        } catch (Exception e) {
            throw new InfrastructureException("Error fetching flight data by airline IDs", e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", apiKey);
        headers.set("Accept", "application/json");
        return headers;
    }

    // Mock data generation methods
    private List<DomainValueFlightSchedule> generateMockFlightSchedules() {
        List<DomainValueFlightSchedule> flights = new ArrayList<>();
        String[] airports = {"JFK", "LAX", "ORD", "DFW", "SFO"};

        for (int i = 0; i < 10; i++) {
            flights.add(DomainValueFlightSchedule.builder()
                    .flightId(UUID.randomUUID().toString())
                    .airlineId(UUID.randomUUID().toString())
                    .flightNumber("FL" + (1000 + i))
                    .origin(airports[i % airports.length])
                    .destination(airports[(i + 1) % airports.length])
                    .departureTime(LocalDateTime.now().plusHours(i).toString())
                    .arrivalTime(LocalDateTime.now().plusHours(i + 2).toString())
                    .status("SCHEDULED")
                    .build());
        }
        return flights;
    }

    private List<DomainValueSeatAvailability> generateMockSeatAvailability() {
        List<DomainValueSeatAvailability> seats = new ArrayList<>();
        String[] classes = {"ECONOMY", "BUSINESS", "FIRST"};

        for (int i = 0; i < 15; i++) {
            seats.add(DomainValueSeatAvailability.builder()
                    .flightId(UUID.randomUUID().toString())
                    .seatClass(classes[i % classes.length])
                    .totalSeats(100 - (i * 20))
                    .availableSeats(50 - (i * 10))
                    .basePrice(200.0 + (i * 100))
                    .currentPrice(180.0 + (i * 100))
                    .isRefundable(i % 2 == 0)
                    .build());
        }
        return seats;
    }

    private List<DomainEntityAirlineInfo> generateMockAirlineInfo() {
        List<DomainEntityAirlineInfo> airlines = new ArrayList<>();
        String[] alliances = {"STAR_ALLIANCE", "ONEWORLD", "SKYTEAM"};

        for (int i = 0; i < 5; i++) {
            airlines.add(DomainEntityAirlineInfo.builder()
                    .airlineId(UUID.randomUUID())
                    .airlineName("Airline " + (i + 1))
                    .iataCode("A" + i)
                    .icaoCode("AAA" + i)
                    .alliance(alliances[i % alliances.length])
                    .country("Country " + (i + 1))
                    .isActive(true)
                    .fleetSize(50 + (i * 10))
                    .build());
        }
        return airlines;
    }

    private List<DomainEntityFlightSchedule> generateMockFlightSchedules(List<UUID> airlineIds) {
        List<DomainEntityFlightSchedule> flights = new ArrayList<>();
        String[] airports = {"JFK", "LAX", "ORD", "DFW", "SFO"};

        for (UUID airlineId : airlineIds) {
            for (int i = 0; i < 3; i++) {
                flights.add(DomainEntityFlightSchedule.builder()
                        .flightId(UUID.randomUUID())
                        .airlineId(airlineId)
                        .flightNumber("FL" + (1000 + i))
                        .origin(airports[i % airports.length])
                        .destination(airports[(i + 1) % airports.length])
                        .departureTime(LocalDateTime.now().plusHours(i))
                        .arrivalTime(LocalDateTime.now().plusHours(i + 2))
                        .status("SCHEDULED")
                        .build());
            }
        }
        return flights;
    }
}
