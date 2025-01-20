package ai.shreds.application.ports;

import ai.shreds.shared.value_objects.SharedIngestionRequestParams;
import ai.shreds.shared.dtos.SharedIngestionResponseDTO;

/**
 * Input port for flight data ingestion operations.
 * Defines the contract for ingesting flight data from external sources.
 */
public interface ApplicationFlightIngestionInputPort {

    /**
     * Initiates the ingestion of flight data for specified airlines.
     *
     * @param params Contains the airline IDs and date range for which to ingest data
     * @return DTO containing the result of the ingestion operation
     * @throws ai.shreds.application.exceptions.ApplicationException if the ingestion process fails
     */
    SharedIngestionResponseDTO ingestFlightData(SharedIngestionRequestParams params);

    /**
     * Retrieves the current status of an ingestion operation.
     *
     * @param ingestionId The unique identifier of the ingestion operation
     * @return DTO containing the current status of the ingestion operation
     * @throws ai.shreds.application.exceptions.ApplicationException if the ingestion ID is not found
     */
    SharedIngestionResponseDTO getIngestionStatus(String ingestionId);
}
