package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.entities.DomainEntityPaymentRecord;
import ai.shreds.domain.entities.DomainEntityPaymentRequest;
import ai.shreds.domain.ports.DomainPortPaymentGateway;
import ai.shreds.domain.ports.DomainPortPaymentRepository;
import ai.shreds.domain.value_objects.DomainValueMoney;
import ai.shreds.infrastructure.exceptions.InfrastructureExceptionPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Implementation of the payment gateway port.
 * Handles interactions with external payment gateway through a client.
 */
@Slf4j
@Service
public class InfrastructurePaymentGatewayImpl implements DomainPortPaymentGateway {

    private final InfrastructurePaymentGatewayClient paymentGatewayClient;
    private final DomainPortPaymentRepository paymentRepository;

    @Autowired
    public InfrastructurePaymentGatewayImpl(InfrastructurePaymentGatewayClient paymentGatewayClient,
                                           DomainPortPaymentRepository paymentRepository) {
        this.paymentGatewayClient = paymentGatewayClient;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public DomainValueMoney authorizePayment(DomainEntityPaymentRequest request) {
        log.debug("Authorizing payment for request: {}", request.getId());
        try {
            DomainValueMoney authorizedAmount = paymentGatewayClient.callGatewayForAuthorization(request);
            log.info("Successfully authorized payment for request: {}", request.getId());
            return authorizedAmount;
        } catch (Exception e) {
            log.error("Failed to authorize payment for request: {}", request.getId(), e);
            throw new InfrastructureExceptionPayment("Failed to authorize payment", e);
        }
    }

    @Override
    public boolean capturePayment(DomainEntityPaymentRecord record) {
        log.debug("Capturing payment for transaction: {}", record.getTransactionId());
        try {
            boolean captured = paymentGatewayClient.callGatewayForCapture(record);
            log.info("Payment capture result for transaction {}: {}", record.getTransactionId(), captured);
            return captured;
        } catch (Exception e) {
            log.error("Failed to capture payment for transaction: {}", record.getTransactionId(), e);
            throw new InfrastructureExceptionPayment("Failed to capture payment", e);
        }
    }

    @Override
    public boolean voidPayment(DomainEntityPaymentRecord record) {
        log.debug("Voiding payment for transaction: {}", record.getTransactionId());
        try {
            boolean voided = paymentGatewayClient.callGatewayForVoid(record);
            log.info("Payment void result for transaction {}: {}", record.getTransactionId(), voided);
            return voided;
        } catch (Exception e) {
            log.error("Failed to void payment for transaction: {}", record.getTransactionId(), e);
            throw new InfrastructureExceptionPayment("Failed to void payment", e);
        }
    }

    @Override
    public boolean refundPayment(DomainEntityPaymentRecord record, DomainValueMoney amount) {
        log.debug("Refunding payment for transaction: {} amount: {}", record.getTransactionId(), amount);
        try {
            boolean refunded = paymentGatewayClient.callGatewayForRefund(record, amount);
            log.info("Payment refund result for transaction {}: {}", record.getTransactionId(), refunded);
            return refunded;
        } catch (Exception e) {
            log.error("Failed to refund payment for transaction: {}", record.getTransactionId(), e);
            throw new InfrastructureExceptionPayment("Failed to refund payment", e);
        }
    }

    @Override
    public boolean verifyPaymentMethod(String paymentMethod, Map<String, Object> params) {
        log.debug("Verifying payment method: {}", paymentMethod);
        try {
            boolean verified = paymentGatewayClient.verifyPaymentMethod(paymentMethod, params);
            log.info("Payment method verification result: {}", verified);
            return verified;
        } catch (Exception e) {
            log.error("Failed to verify payment method: {}", paymentMethod, e);
            throw new InfrastructureExceptionPayment("Failed to verify payment method", e);
        }
    }

    @Override
    public String getTransactionStatus(UUID transactionId) {
        log.debug("Getting transaction status for: {}", transactionId);
        try {
            String status = paymentGatewayClient.getTransactionStatus(transactionId);
            log.info("Retrieved status for transaction {}: {}", transactionId, status);
            return status;
        } catch (Exception e) {
            log.error("Failed to get transaction status for: {}", transactionId, e);
            throw new InfrastructureExceptionPayment("Failed to get transaction status", e);
        }
    }

    @Override
    public boolean supportsPaymentMethod(String paymentMethod) {
        log.debug("Checking support for payment method: {}", paymentMethod);
        try {
            boolean supported = paymentGatewayClient.isPaymentMethodSupported(paymentMethod);
            log.info("Payment method {} support status: {}", paymentMethod, supported);
            return supported;
        } catch (Exception e) {
            log.error("Failed to check payment method support: {}", paymentMethod, e);
            throw new InfrastructureExceptionPayment("Failed to check payment method support", e);
        }
    }

    /**
     * Returns the gateway reference for a given transaction ID by looking up the
     * corresponding payment record and using its request ID.
     *
     * @param transactionId The UUID transaction ID.
     * @return The gateway reference string.
     */
    public String getGatewayReference(UUID transactionId) {
        log.debug("Getting gateway reference for transaction: {}", transactionId);
        try {
            DomainEntityPaymentRecord record = paymentRepository.findPaymentRecordByTransactionId(transactionId)
                    .orElseThrow(() -> new InfrastructureExceptionPayment("No PaymentRecord found for transactionId: " + transactionId));

            Long requestId = record.getRequestId();
            String reference = paymentGatewayClient.getGatewayReference(requestId);
            log.info("Retrieved gateway reference for transaction {}: {}", transactionId, reference);
            return reference;
        } catch (Exception e) {
            log.error("Failed to get gateway reference for transaction: {}", transactionId, e);
            throw new InfrastructureExceptionPayment("Failed to get gateway reference", e);
        }
    }
}
