package ai.shreds.application.services;

import ai.shreds.application.exceptions.ApplicationPaymentException;
import ai.shreds.application.ports.ApplicationPaymentInputPort;
import ai.shreds.application.ports.ApplicationPaymentOutputPort;
import ai.shreds.shared.SharedPaymentEventDTO;
import ai.shreds.shared.SharedPaymentRequestParams;
import ai.shreds.shared.SharedPaymentResponseDTO;
import ai.shreds.shared.SharedPaymentStatusResponseDTO;
import ai.shreds.domain.services.DomainServicePayment;
import ai.shreds.domain.entities.DomainEntityPaymentRequest;
import ai.shreds.domain.entities.DomainEntityPaymentRecord;
import ai.shreds.domain.entities.DomainEntityPaymentStatus;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Application service implementing the payment operations.
 * Coordinates between the domain layer and external interfaces.
 */
@Slf4j
@Service
public class ApplicationPaymentService implements ApplicationPaymentInputPort {

    private final DomainServicePayment domainServicePayment;
    private final ApplicationMapperPayment mapper;
    private final ApplicationPaymentOutputPort paymentOutputPort;

    public ApplicationPaymentService(DomainServicePayment domainServicePayment,
                                   ApplicationMapperPayment mapper,
                                   ApplicationPaymentOutputPort paymentOutputPort) {
        this.domainServicePayment = domainServicePayment;
        this.mapper = mapper;
        this.paymentOutputPort = paymentOutputPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public SharedPaymentResponseDTO createPayment(SharedPaymentRequestParams params) {
        log.info("Creating payment for user: {} with amount: {} {}", 
                params.getUserId(), params.getAmount(), params.getCurrency());
        
        validatePaymentRequest(params);
        DomainEntityPaymentRequest domainRequest = mapper.toDomainEntity(params);
        DomainEntityPaymentRecord record = domainServicePayment.processPayment(domainRequest);

        // Publish payment status event
        publishPaymentStatusEvent(record);

        SharedPaymentResponseDTO response = mapper.toSharedDTO(record);
        log.info("Payment created successfully with transaction ID: {}", response.getTransactionId());
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public SharedPaymentStatusResponseDTO getPaymentStatus(UUID transactionId) {
        log.info("Retrieving payment status for transaction: {}", transactionId);
        
        DomainEntityPaymentStatus domainStatus = domainServicePayment.getPaymentStatus(transactionId);
        DomainEntityPaymentRecord domainRecord = domainServicePayment.getPaymentRecordByTransactionId(transactionId);
        
        SharedPaymentStatusResponseDTO response = mapper.toSharedStatusDTO(
                domainStatus, domainRecord.getUserId(), domainRecord.getTransactionId());
        
        log.info("Retrieved status: {} for transaction: {}", response.getStatus(), transactionId);
        return response;
    }

    /**
     * Validates the payment request parameters
     * @param params The payment request parameters to validate
     * @throws ApplicationPaymentException if validation fails
     */
    private void validatePaymentRequest(SharedPaymentRequestParams params) {
        log.debug("Validating payment request parameters");
        
        if (params == null) {
            throw new ApplicationPaymentException("Payment request params cannot be null");
        }
        if (params.getAmount() == null || params.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApplicationPaymentException("Payment amount must be greater than zero");
        }
        if (params.getCurrency() == null || params.getCurrency().trim().isEmpty()) {
            throw new ApplicationPaymentException("Currency must not be null or empty");
        }
        if (params.getPaymentMethod() == null || params.getPaymentMethod().trim().isEmpty()) {
            throw new ApplicationPaymentException("Payment method must not be null or empty");
        }
        if (params.getUserId() == null) {
            throw new ApplicationPaymentException("User ID must not be null");
        }
        
        log.debug("Payment request parameters validated successfully");
    }

    /**
     * Publishes a payment status event to the message queue
     * @param record The payment record containing the status information
     */
    private void publishPaymentStatusEvent(DomainEntityPaymentRecord record) {
        try {
            SharedPaymentEventDTO event = SharedPaymentEventDTO.of(
                    record.getTransactionId(),
                    record.getStatus().getValue());
            
            paymentOutputPort.publishPaymentEvent(event);
            log.info("Published payment status event for transaction: {}", record.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to publish payment status event for transaction: {}", 
                    record.getTransactionId(), e);
            // We might want to handle this differently depending on requirements
            // For now, we'll just log it and continue
        }
    }

    /**
     * Retrieves a payment record by its transaction ID
     * @param transactionId The transaction ID to look up
     * @return The payment record
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public DomainEntityPaymentRecord getPaymentRecordByTransactionId(UUID transactionId) {
        log.debug("Retrieving payment record for transaction: {}", transactionId);
        return domainServicePayment.getPaymentRecordByTransactionId(transactionId);
    }
}
