package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.entities.DomainEntityFlightSchedule;
import ai.shreds.domain.ports.DomainExternalAirlinePort;
import ai.shreds.infrastructure.exceptions.InfrastructureException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

/**
 * Implementation of the external airline port.
 * Handles communication with external airline APIs with retry and error handling.
 */
@Service
public class InfrastructureExternalAirlineClientImpl implements DomainExternalAirlinePort {

    private final RestTemplate restTemplate;
    private final RetryTemplate retryTemplate;
    private final String apiBaseUrl;
    private final String apiKey;

    public InfrastructureExternalAirlineClientImpl(
            RestTemplate restTemplate,
            @Value("${airline.api.base-url}") String apiBaseUrl,
            @Value("${airline.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiBaseUrl = apiBaseUrl;
        this.apiKey = apiKey;
        this.retryTemplate = createRetryTemplate();
    }

    @Override
    public Object callAirlineAPI(Object params) {
        return retryTemplate.execute(context -> {
            try {
                HttpHeaders headers = createHeaders();
                HttpEntity<Object> request = new HttpEntity<>(params, headers);
                
                ResponseEntity<Object> response = restTemplate.exchange(
                        apiBaseUrl + "/api/v1/flights",
                        HttpMethod.POST,
                        request,
                        Object.class
                );

                if (response.getStatusCode() == HttpStatus.OK) {
                    return response.getBody();
                } else {
                    throw new InfrastructureException(
                            "Unexpected response status: " + response.getStatusCode());
                }
            } catch (HttpStatusCodeException e) {
                handleHttpError(e);
                throw e;
            } catch (Exception e) {
                handleApiError(e);
                throw new InfrastructureException("Error calling airline API", e);
            }
        });
    }

    @Override
    public List<DomainEntityFlightSchedule> fetchFlightData(List<UUID> airlineIds) {
        return retryTemplate.execute(context -> {
            try {
                HttpHeaders headers = createHeaders();
                String url = apiBaseUrl + "/api/v1/airlines/flights?ids=" + 
                        String.join(",", airlineIds.stream().map(UUID::toString).toList());

                ResponseEntity<FlightDataResponse[]> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        FlightDataResponse[].class
                );

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    return convertToFlightSchedules(response.getBody());
                } else {
                    // For demo purposes, return mock data
                    return generateMockFlightData(airlineIds);
                }
            } catch (HttpStatusCodeException e) {
                handleHttpError(e);
                throw e;
            } catch (Exception e) {
                handleApiError(e);
                throw new InfrastructureException("Error fetching flight data", e);
            }
        });
    }

    private RetryTemplate createRetryTemplate() {
        RetryTemplate template = new RetryTemplate();

        // Configure exponential backoff
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000L);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(10000L);
        template.setBackOffPolicy(backOffPolicy);

        // Configure retry policy
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(HttpStatusCodeException.class, true);
        retryableExceptions.put(Exception.class, true);
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions);
        template.setRetryPolicy(retryPolicy);

        return template;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private void handleHttpError(HttpStatusCodeException e) {
        switch (e.getStatusCode()) {
            case UNAUTHORIZED:
                throw new InfrastructureException("API authentication failed", e);
            case NOT_FOUND:
                throw new InfrastructureException("Requested resource not found", e);
            case TOO_MANY_REQUESTS:
                throw new InfrastructureException("Rate limit exceeded", e);
            default:
                throw new InfrastructureException(
                        "HTTP error: " + e.getStatusCode() + " - " + e.getStatusText(), e);
        }
    }

    private void handleApiError(Exception e) {
        // Log the error
        // logger.error("API call failed", e);

        // Implement custom error handling logic
        if (e instanceof HttpStatusCodeException) {
            handleHttpError((HttpStatusCodeException) e);
        } else {
            throw new InfrastructureException("Unexpected error in API call", e);
        }
    }

    // Mock data generation for demonstration
    private List<DomainEntityFlightSchedule> generateMockFlightData(List<UUID> airlineIds) {
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

    // Helper class for JSON deserialization
    private static class FlightDataResponse {
        private String flightId;
        private String airlineId;
        private String flightNumber;
        private String origin;
        private String destination;
        private String departureTime;
        private String arrivalTime;
        private String status;
        // Add getters and setters
    }

    private List<DomainEntityFlightSchedule> convertToFlightSchedules(FlightDataResponse[] responses) {
        List<DomainEntityFlightSchedule> flights = new ArrayList<>();
        for (FlightDataResponse response : responses) {
            flights.add(DomainEntityFlightSchedule.builder()
                    .flightId(UUID.fromString(response.flightId))
                    .airlineId(UUID.fromString(response.airlineId))
                    .flightNumber(response.flightNumber)
                    .origin(response.origin)
                    .destination(response.destination)
                    .departureTime(LocalDateTime.parse(response.departureTime))
                    .arrivalTime(LocalDateTime.parse(response.arrivalTime))
                    .status(response.status)
                    .build());
        }
        return flights;
    }
}
