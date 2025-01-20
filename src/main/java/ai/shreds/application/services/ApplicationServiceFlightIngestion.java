package ai.shreds.application.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

import ai.shreds.shared.value_objects.SharedIngestionRequestParams;
import ai.shreds.shared.dtos.SharedIngestionResponseDTO;
import ai.shreds.shared.enums.SharedEnumIngestionStatus;
import ai.shreds.domain.ports.DomainPortFlightRepository;
import ai.shreds.domain.ports.DomainPortSeatAvailabilityRepository;
import ai.shreds.domain.ports.DomainPortAirlineInfoRepository;
import ai.shreds.domain.ports.DomainPortFlightDataSync;
import ai.shreds.domain.services.DomainDataValidationService;
import ai.shreds.domain.services.DomainServiceFlightSynchronization;
import ai.shreds.domain.entities.DomainEntityIngestionStatus;
import ai.shreds.application.ports.ApplicationFlightIngestionInputPort;
import ai.shreds.application.exceptions.ApplicationException;

/**
 * Service handling the ingestion of flight data from external sources.
 * Implements ApplicationFlightIngestionInputPort for flight data ingestion operations.
 */
public class ApplicationServiceFlightIngestion implements ApplicationFlightIngestionInputPort {

    private final DomainDataValidationService domainDataValidationService;
    private final DomainPortFlightDataSync domainPortFlightDataSync;
    private final DomainPortFlightRepository domainPortFlightRepository;
    private final DomainPortSeatAvailabilityRepository domainPortSeatAvailabilityRepository;
    private final DomainPortAirlineInfoRepository domainPortAirlineInfoRepository;
    private final DomainServiceFlightSynchronization domainServiceFlightSynchronization;

    // In-memory store for ingestion status tracking
    private final ConcurrentHashMap<String, DomainEntityIngestionStatus> ingestionStatusMap;

    public ApplicationServiceFlightIngestion(
            DomainDataValidationService domainDataValidationService,
            DomainPortFlightDataSync domainPortFlightDataSync,
            DomainPortFlightRepository domainPortFlightRepository,
            DomainPortSeatAvailabilityRepository domainPortSeatAvailabilityRepository,
            DomainPortAirlineInfoRepository domainPortAirlineInfoRepository,
            DomainServiceFlightSynchronization domainServiceFlightSynchronization) {
        this.domainDataValidationService = domainDataValidationService;
        this.domainPortFlightDataSync = domainPortFlightDataSync;
        this.domainPortFlightRepository = domainPortFlightRepository;
        this.domainPortSeatAvailabilityRepository = domainPortSeatAvailabilityRepository;
        this.domainPortAirlineInfoRepository = domainPortAirlineInfoRepository;
        this.domainServiceFlightSynchronization = domainServiceFlightSynchronization;
        this.ingestionStatusMap = new ConcurrentHashMap<>();
    }

    @Override
    public SharedIngestionResponseDTO ingestFlightData(SharedIngestionRequestParams params) {
        // Generate unique ingestion ID
        String ingestionId = UUID.randomUUID().toString();

        try {
            // Validate input parameters
            if (params.getAirlineIds() == null || params.getAirlineIds().isEmpty()) {
                throw new ApplicationException("No airline IDs provided for ingestion");
            }

            // Convert incoming airlineIds to UUID and validate
            List<UUID> airlineIds = params.getAirlineIds().stream()
                    .map(id -> {
                        try {
                            return UUID.fromString(id);
                        } catch (IllegalArgumentException e) {
                            throw new ApplicationException("Invalid airline ID format: " + id);
                        }
                    })
                    .collect(Collectors.toList());

            // Initialize ingestion status
            DomainEntityIngestionStatus ingestionStatus = DomainEntityIngestionStatus.builder()
                    .ingestionId(UUID.fromString(ingestionId))
                    .airlineIds(airlineIds)
                    .startTime(LocalDateTime.now())
                    .lastUpdated(LocalDateTime.now())
                    .status("IN_PROGRESS")
                    .message("Initiating flight data ingestion")
                    .totalRecords(0)
                    .processedRecords(0)
                    .build();

            ingestionStatusMap.put(ingestionId, ingestionStatus);

            // Start asynchronous ingestion process
            new Thread(() -> processIngestion(ingestionId, airlineIds)).start();

            // Return initial response
            return SharedIngestionResponseDTO.builder()
                    .status(SharedEnumIngestionStatus.IN_PROGRESS)
                    .message("Ingestion process started. Use ingestion ID: " + ingestionId + " to check status.")
                    .ingestedAirlines(params.getAirlineIds())
                    .build();

        } catch (Exception e) {
            DomainEntityIngestionStatus failedStatus = DomainEntityIngestionStatus.builder()
                    .ingestionId(UUID.fromString(ingestionId))
                    .status("FAILED")
                    .message(e.getMessage())
                    .startTime(LocalDateTime.now())
                    .lastUpdated(LocalDateTime.now())
                    .build();

            ingestionStatusMap.put(ingestionId, failedStatus);

            throw new ApplicationException("Error initiating flight data ingestion: " + e.getMessage());
        }
    }

    @Override
    public SharedIngestionResponseDTO getIngestionStatus(String ingestionId) {
        DomainEntityIngestionStatus status = ingestionStatusMap.get(ingestionId);
        if (status == null) {
            throw new ApplicationException("Ingestion ID not found: " + ingestionId);
        }

        return SharedIngestionResponseDTO.builder()
                .status(SharedEnumIngestionStatus.fromValue(status.getStatus()))
                .message(String.format("%s - Progress: %d%%", status.getMessage(), status.getProgressPercentage()))
                .ingestedAirlines(status.getAirlineIds().stream().map(UUID::toString).collect(Collectors.toList()))
                .build();
    }

    private void processIngestion(String ingestionId, List<UUID> airlineIds) {
        DomainEntityIngestionStatus status = ingestionStatusMap.get(ingestionId);

        try {
            // Trigger synchronization logic in the domain
            domainServiceFlightSynchronization.synchronizeFlightData(airlineIds);

            // Update status on successful completion
            status.markComplete("Flight data ingestion completed successfully");
            ingestionStatusMap.put(ingestionId, status);

        } catch (Exception e) {
            // Update status on failure
            status.markFailed("Error during flight data ingestion: " + e.getMessage());
            ingestionStatusMap.put(ingestionId, status);
        }
    }
}
