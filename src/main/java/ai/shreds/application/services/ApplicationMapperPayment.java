package ai.shreds.application.services;

import ai.shreds.shared.SharedPaymentRequestParams;
import ai.shreds.shared.SharedPaymentResponseDTO;
import ai.shreds.shared.SharedPaymentStatusResponseDTO;
import ai.shreds.domain.entities.DomainEntityPaymentRequest;
import ai.shreds.domain.entities.DomainEntityPaymentRecord;
import ai.shreds.domain.entities.DomainEntityPaymentStatus;
import org.mapstruct.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MapStruct mapper for converting between domain entities and DTOs.
 * Handles all object mappings in the application layer.
 */
@Mapper(componentModel = "spring", imports = {LocalDateTime.class})
public interface ApplicationMapperPayment {

    /**
     * Maps payment request parameters to domain entity
     * @param params The payment request parameters
     * @return Domain payment request entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestTimestamp", expression = "java(LocalDateTime.now())")
    DomainEntityPaymentRequest toDomainEntity(SharedPaymentRequestParams params);

    /**
     * Maps domain payment record to shared DTO
     * @param record The domain payment record
     * @return Payment response DTO
     */
    @Mapping(target = "status", expression = "java(record.getStatus() != null ? record.getStatus().getValue() : null)")
    @Mapping(target = "error", ignore = true)
    @Mapping(target = "fraudAlert", ignore = true)
    SharedPaymentResponseDTO toSharedDTO(DomainEntityPaymentRecord record);

    /**
     * Maps domain payment status to shared status DTO
     * @param status The domain payment status
     * @param userId The user ID associated with the payment
     * @param transactionId The transaction ID
     * @return Payment status response DTO
     */
    @Mapping(target = "status", expression = "java(status.getStatus().getValue())")
    @Mapping(target = "updatedAt", source = "status.statusChangedAt")
    SharedPaymentStatusResponseDTO toSharedStatusDTO(DomainEntityPaymentStatus status, Long userId, UUID transactionId);

    /**
     * Handles null input safely
     * @param params The input parameters
     * @return null if input is null
     */
    @BeforeMapping
    default void handleNull(Object params) {
        if (params == null) {
            throw new IllegalArgumentException("Input parameter cannot be null");
        }
    }

    /**
     * After mapping callback for SharedPaymentResponseDTO
     * @param record The source domain record
     * @param dto The target DTO
     */
    @AfterMapping
    default void afterMapping(@MappingTarget SharedPaymentResponseDTO dto, DomainEntityPaymentRecord record) {
        if (record.getStatus() != null && record.getStatus().isFailure()) {
            dto.setError("Payment processing failed");
        }
    }
}
