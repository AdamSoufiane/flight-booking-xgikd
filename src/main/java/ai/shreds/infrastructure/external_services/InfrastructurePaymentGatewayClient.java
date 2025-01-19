package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.entities.DomainEntityPaymentRecord;
import ai.shreds.domain.entities.DomainEntityPaymentRequest;
import ai.shreds.domain.value_objects.DomainValueMoney;
import ai.shreds.infrastructure.exceptions.InfrastructureExceptionPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client for external payment gateway interactions.
 * Currently implements mock behavior for testing purposes.
 */
@Slf4j
@Component
public class InfrastructurePaymentGatewayClient {

    // We store references keyed by the request ID which is a Long.
    private final Map<Long, String> gatewayReferences = new ConcurrentHashMap<>();

    // We store transaction statuses keyed by the transaction UUID.
    private final Map<UUID, String> transactionStatuses = new ConcurrentHashMap<>();

    /**
     * Calls gateway for payment authorization
     * @param request The payment request to authorize
     * @return Authorized money amount
     */
    public DomainValueMoney callGatewayForAuthorization(DomainEntityPaymentRequest request) {
        log.debug("Simulating gateway authorization for request: {}", request.getId());
        try {
            // Simulate gateway processing time
            Thread.sleep(100);

            // Generate a mock gateway reference
            String gatewayRef = "GW-" + UUID.randomUUID();
            gatewayReferences.put(request.getId(), gatewayRef);

            // Simulate successful authorization
            return new DomainValueMoney(request.getAmount(), request.getCurrency());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw handleGatewayError(e);
        }
    }

    /**
     * Calls gateway for payment capture
     * @param record The payment record to capture
     * @return true if capture was successful
     */
    public boolean callGatewayForCapture(DomainEntityPaymentRecord record) {
        log.debug("Simulating gateway capture for transaction: {}", record.getTransactionId());
        try {
            Thread.sleep(100);
            transactionStatuses.put(record.getTransactionId(), "CAPTURED");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw handleGatewayError(e);
        }
    }

    /**
     * Calls gateway to void a payment
     * @param record The payment record to void
     * @return true if void was successful
     */
    public boolean callGatewayForVoid(DomainEntityPaymentRecord record) {
        log.debug("Simulating gateway void for transaction: {}", record.getTransactionId());
        try {
            Thread.sleep(100);
            transactionStatuses.put(record.getTransactionId(), "VOIDED");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw handleGatewayError(e);
        }
    }

    /**
     * Calls gateway for payment refund
     * @param record The payment record to refund
     * @param amount The amount to refund
     * @return true if refund was successful
     */
    public boolean callGatewayForRefund(DomainEntityPaymentRecord record, DomainValueMoney amount) {
        log.debug("Simulating gateway refund for transaction: {}", record.getTransactionId());
        try {
            Thread.sleep(100);
            transactionStatuses.put(record.getTransactionId(), "REFUNDED");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw handleGatewayError(e);
        }
    }

    /**
     * Verifies a payment method with the gateway
     * @param paymentMethod The payment method to verify
     * @param params Additional verification parameters
     * @return true if payment method is valid
     */
    public boolean verifyPaymentMethod(String paymentMethod, Map<String, Object> params) {
        log.debug("Simulating payment method verification: {}", paymentMethod);
        // Mock implementation - accept common payment methods
        return "CREDIT_CARD".equals(paymentMethod) ||
               "DEBIT_CARD".equals(paymentMethod) ||
               "PAYPAL".equals(paymentMethod);
    }

    /**
     * Gets transaction status from gateway
     * @param transactionId The transaction ID
     * @return Current transaction status
     */
    public String getTransactionStatus(UUID transactionId) {
        log.debug("Getting transaction status for: {}", transactionId);
        return transactionStatuses.getOrDefault(transactionId, "UNKNOWN");
    }

    /**
     * Checks if payment method is supported
     * @param paymentMethod The payment method to check
     * @return true if payment method is supported
     */
    public boolean isPaymentMethodSupported(String paymentMethod) {
        log.debug("Checking support for payment method: {}", paymentMethod);
        // Mock implementation - support common payment methods
        return "CREDIT_CARD".equals(paymentMethod) ||
               "DEBIT_CARD".equals(paymentMethod) ||
               "PAYPAL".equals(paymentMethod);
    }

    /**
     * Gets gateway reference for transaction
     * @param requestId The payment request ID
     * @return Gateway reference
     */
    public String getGatewayReference(Long requestId) {
        log.debug("Getting gateway reference for request ID: {}", requestId);
        return gatewayReferences.getOrDefault(requestId, "UNKNOWN");
    }

    /**
     * Handles gateway errors
     * @param error The error to handle
     * @return InfrastructureExceptionPayment
     */
    private InfrastructureExceptionPayment handleGatewayError(Exception error) {
        log.error("Gateway error occurred", error);
        return new InfrastructureExceptionPayment("Gateway error: " + error.getMessage(), error);
    }
}
