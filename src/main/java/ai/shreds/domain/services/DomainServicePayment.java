package ai.shreds.domain.services;

import ai.shreds.domain.ports.DomainPortPaymentRepository;
import ai.shreds.domain.ports.DomainPortPaymentGateway;
import ai.shreds.domain.ports.DomainPortPaymentFraud;
import ai.shreds.domain.ports.DomainPortPaymentMessagePublisher;
import ai.shreds.domain.entities.DomainEntityPaymentRequest;
import ai.shreds.domain.entities.DomainEntityPaymentRecord;
import ai.shreds.domain.entities.DomainEntityPaymentStatus;
import ai.shreds.domain.exceptions.DomainExceptionPayment;
import ai.shreds.domain.value_objects.DomainValueMoney;
import ai.shreds.shared.SharedEnumPaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Core domain service for payment processing.
 * Handles payment lifecycle, authorization, and status management.
 */
@Slf4j
@Service
public class DomainServicePayment {

    private final DomainPortPaymentRepository paymentRepository;
    private final DomainPortPaymentGateway paymentGateway;
    private final DomainPortPaymentFraud fraudService;
    private final DomainPortPaymentMessagePublisher messagePublisher;
    private final DomainServiceFraudCheck domainServiceFraudCheck;

    public DomainServicePayment(
            DomainPortPaymentRepository paymentRepository,
            DomainPortPaymentGateway paymentGateway,
            DomainPortPaymentFraud fraudService,
            DomainPortPaymentMessagePublisher messagePublisher,
            DomainServiceFraudCheck domainServiceFraudCheck) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.fraudService = fraudService;
        this.messagePublisher = messagePublisher;
        this.domainServiceFraudCheck = domainServiceFraudCheck;
    }

    /**
     * Processes a new payment request
     * @param request The payment request to process
     * @return The processed payment record
     */
    @Transactional
    public DomainEntityPaymentRecord processPayment(DomainEntityPaymentRequest request) {
        log.info("Processing payment request for user: {}", request.getUserId());
        try {
            request.validate();
            DomainEntityPaymentRequest savedRequest = paymentRepository.savePaymentRequest(request);
            log.debug("Saved payment request with ID: {}", savedRequest.getId());

            DomainEntityPaymentRecord record = initializePaymentRecord(savedRequest);
            log.debug("Initialized payment record with transaction ID: {}", record.getTransactionId());

            performFraudCheck(savedRequest);
            DomainValueMoney authorizedMoney = authorizePayment(savedRequest);
            record.setAuthorizedAmount(authorizedMoney);

            DomainEntityPaymentRecord finalRecord = updatePaymentRecord(record, SharedEnumPaymentStatus.AUTHORIZED);
            log.info("Successfully processed payment with transaction ID: {}", finalRecord.getTransactionId());
            return finalRecord;

        } catch (Exception e) {
            log.error("Failed to process payment for user: {}", request.getUserId(), e);
            throw new DomainExceptionPayment("Payment processing failed", e);
        }
    }

    /**
     * Updates a payment record's status
     * @param record The record to update
     * @param newStatus The new status
     * @return The updated record
     */
    @Transactional
    public DomainEntityPaymentRecord updatePaymentRecord(DomainEntityPaymentRecord record, SharedEnumPaymentStatus newStatus) {
        log.info("Updating payment record {} to status {}", record.getTransactionId(), newStatus);
        try {
            record.updateStatus(newStatus);
            DomainEntityPaymentRecord updated = paymentRepository.savePaymentRecord(record);

            DomainEntityPaymentStatus statusEntity = record.toStatusEntity(newStatus);
            paymentRepository.savePaymentStatus(statusEntity);
            messagePublisher.publishPaymentStatus(statusEntity);

            log.info("Successfully updated payment record status: {}", updated.getTransactionId());
            return updated;

        } catch (Exception e) {
            log.error("Failed to update payment record: {}", record.getTransactionId(), e);
            throw new DomainExceptionPayment("Failed to update payment record", e);
        }
    }

    /**
     * Retrieves payment status
     * @param transactionId The transaction ID
     * @return The current payment status
     */
    @Transactional(readOnly = true)
    public DomainEntityPaymentStatus getPaymentStatus(UUID transactionId) {
        log.debug("Retrieving payment status for transaction: {}", transactionId);
        try {
            DomainEntityPaymentRecord record = getPaymentRecordByTransactionId(transactionId);
            return record.toStatusEntity(record.getStatus());
        } catch (Exception e) {
            log.error("Failed to retrieve payment status for transaction: {}", transactionId, e);
            throw new DomainExceptionPayment("Failed to retrieve payment status", e);
        }
    }

    /**
     * Retrieves a payment record
     * @param transactionId The transaction ID
     * @return The payment record
     */
    @Transactional(readOnly = true)
    public DomainEntityPaymentRecord getPaymentRecordByTransactionId(UUID transactionId) {
        log.debug("Retrieving payment record for transaction: {}", transactionId);
        return paymentRepository.findPaymentRecordByTransactionId(transactionId)
                .orElseThrow(() -> new DomainExceptionPayment("Payment record not found: " + transactionId));
    }

    /**
     * Captures an authorized payment
     * @param transactionId The transaction ID
     * @return The updated payment record
     */
    @Transactional
    public DomainEntityPaymentRecord capturePayment(UUID transactionId) {
        log.info("Capturing payment for transaction: {}", transactionId);
        DomainEntityPaymentRecord record = getPaymentRecordByTransactionId(transactionId);

        if (!record.canCapture()) {
            throw new DomainExceptionPayment("Payment cannot be captured in current state: " + record.getStatus());
        }

        try {
            if (paymentGateway.capturePayment(record)) {
                return updatePaymentRecord(record, SharedEnumPaymentStatus.COMPLETED);
            } else {
                return updatePaymentRecord(record, SharedEnumPaymentStatus.FAILED);
            }
        } catch (Exception e) {
            log.error("Failed to capture payment: {}", transactionId, e);
            updatePaymentRecord(record, SharedEnumPaymentStatus.FAILED);
            throw new DomainExceptionPayment("Payment capture failed", e);
        }
    }

    private DomainEntityPaymentRecord initializePaymentRecord(DomainEntityPaymentRequest request) {
        DomainEntityPaymentRecord record = request.toRecordEntity();
        record.setStatus(SharedEnumPaymentStatus.PENDING);
        return paymentRepository.savePaymentRecord(record);
    }

    private void performFraudCheck(DomainEntityPaymentRequest request) {
        log.debug("Performing fraud check for request: {}", request.getId());
        if (domainServiceFraudCheck.performFraudCheck(request)) {
            log.warn("Fraud detected for request: {}", request.getId());
            throw new DomainExceptionPayment("Fraud check failed");
        }
    }

    private DomainValueMoney authorizePayment(DomainEntityPaymentRequest request) {
        log.debug("Authorizing payment for request: {}", request.getId());
        DomainValueMoney authorizedMoney = paymentGateway.authorizePayment(request);
        if (authorizedMoney == null) {
            log.error("Payment authorization failed for request: {}", request.getId());
            throw new DomainExceptionPayment("Payment authorization failed");
        }
        return authorizedMoney;
    }
}
