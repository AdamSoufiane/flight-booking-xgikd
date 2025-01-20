package ai.shreds.application.services;

import ai.shreds.application.exceptions.ApplicationException;
import ai.shreds.application.ports.ApplicationFlightIngestionInputPort;
import ai.shreds.domain.entities.DomainEntityFlightSchedule;
import ai.shreds.domain.ports.*;
import ai.shreds.domain.services.DomainDataValidationService;
import ai.shreds.domain.services.DomainServiceFlightSynchronization;
import ai.shreds.shared.value_objects.SharedIngestionRequestParams;
import ai.shreds.shared.dtos.SharedIngestionResponseDTO;
import ai.shreds.shared.enums.SharedEnumIngestionStatus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application service implementing flight data ingestion operations.
 */
@Service
@Transactional
public class ApplicationFlightIngestionService implements ApplicationFlightIngestionInputPort {

    private final DomainDataValidationService domainDataValidationService;
    private final DomainPortFlightDataSync domainPortFlightDataSync;
    private final DomainPortFlightRepository domainPortFlightRepository;
    private final DomainPortSeatAvailabilityRepository domainPortSeatAvailabilityRepository;
    private final DomainPortAirlineInfoRepository domainPortAirlineInfoRepository;
    private final DomainServiceFlightSynchronization domainServiceFlightSynchronization;

    // In-memory store for ingestion status tracking
    private final ConcurrentHashMap<String, IngestionStatus> ingestionStatusMap;

    public ApplicationFlightIngestionService(
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
        String ingestionId = UUID.randomUUID().toString();

        try {
            validateIngestionParams(params);

            // Initialize ingestion status
            IngestionStatus status = new IngestionStatus(ingestionId, params.getAirlineIds());
            ingestionStatusMap.put(ingestionId, status);

            // Start asynchronous ingestion
            new Thread(() -> processIngestion(ingestionId, params)).start();

            return SharedIngestionResponseDTO.builder()
                    .status(SharedEnumIngestionStatus.IN_PROGRESS)
                    .message("Ingestion process started. Use ingestion ID: " + ingestionId + " to check status.")
                    .ingestedAirlines(params.getAirlineIds())
                    .build();

        } catch (Exception e) {
            IngestionStatus failedStatus = new IngestionStatus(ingestionId, params.getAirlineIds());
            failedStatus.markFailed(e.getMessage());
            ingestionStatusMap.put(ingestionId, failedStatus);

            throw new ApplicationException("Error initiating flight data ingestion: " + e.getMessage(), e);
        }
    }

    @Override
    public SharedIngestionResponseDTO getIngestionStatus(String ingestionId) {
        IngestionStatus status = ingestionStatusMap.get(ingestionId);
        if (status == null) {
            throw new ApplicationException("Ingestion ID not found: " + ingestionId);
        }

        return SharedIngestionResponseDTO.builder()
                .status(SharedEnumIngestionStatus.fromValue(status.getStatus()))
                .message(String.format("%s - Progress: %d%%", status.getMessage(), status.getProgressPercentage()))
                .ingestedAirlines(status.getAirlineIds())
                .build();
    }

    private void validateIngestionParams(SharedIngestionRequestParams params) {
        if (params == null) {
            throw new ApplicationException("Ingestion parameters cannot be null");
        }
        if (params.getAirlineIds() == null || params.getAirlineIds().isEmpty()) {
            throw new ApplicationException("No airline IDs provided for ingestion");
        }
        if (params.getDateRange() == null || !params.getDateRange().matches("\\d{4}-\\d{2}-\\d{2}/\\d{4}-\\d{2}-\\d{2}")) {
            throw new ApplicationException("Invalid date range format. Expected: YYYY-MM-DD/YYYY-MM-DD");
        }
    }

    private void processIngestion(String ingestionId, SharedIngestionRequestParams params) {
        IngestionStatus status = ingestionStatusMap.get(ingestionId);

        try {
            List<UUID> airlineIds = params.getAirlineIds().stream()
                    .map(UUID::fromString)
                    .toList();

            status.updateProgress(10, "Fetching airline information");
            domainServiceFlightSynchronization.synchronizeFlightData(airlineIds);

            status.updateProgress(100, "Flight data ingestion completed successfully");
            status.markComplete();

        } catch (Exception e) {
            status.markFailed("Error during flight data ingestion: " + e.getMessage());
            throw new ApplicationException("Flight data ingestion failed", e);
        }
    }

    /**
     * Internal class to track ingestion status.
     */
    private static class IngestionStatus {
        private final String id;
        private final List<String> airlineIds;
        private final LocalDateTime startTime;
        private LocalDateTime lastUpdated;
        private String status;
        private String message;
        private int progress;

        public IngestionStatus(String id, List<String> airlineIds) {
            this.id = id;
            this.airlineIds = airlineIds;
            this.startTime = LocalDateTime.now();
            this.lastUpdated = LocalDateTime.now();
            this.status = "IN_PROGRESS";
            this.message = "Initializing ingestion process";
            this.progress = 0;
        }

        public void updateProgress(int progress, String message) {
            this.progress = progress;
            this.message = message;
            this.lastUpdated = LocalDateTime.now();
        }

        public void markComplete() {
            this.status = "COMPLETED";
            this.progress = 100;
            this.message = "Ingestion completed successfully";
            this.lastUpdated = LocalDateTime.now();
        }

        public void markFailed(String error) {
            this.status = "FAILED";
            this.message = error;
            this.lastUpdated = LocalDateTime.now();
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public int getProgressPercentage() { return progress; }
        public List<String> getAirlineIds() { return airlineIds; }
    }
}
