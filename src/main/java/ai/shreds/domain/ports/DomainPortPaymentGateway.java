package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityPaymentRequest;
import ai.shreds.domain.entities.DomainEntityPaymentRecord;
import ai.shreds.domain.exceptions.DomainExceptionPayment;
import ai.shreds.domain.value_objects.DomainValueMoney;

import java.util.Map;
import java.util.UUID;

/**
 * Port for interacting with external payment gateway.
 * Defines the contract for payment processing operations with external payment providers.
 */
public interface DomainPortPaymentGateway {

    /**
     * Authorizes a payment with the payment gateway
     * @param request The payment request to authorize
     * @return The authorized amount and currency
     * @throws DomainExceptionPayment if authorization fails
     */
    DomainValueMoney authorizePayment(DomainEntityPaymentRequest request);

    /**
     * Captures a previously authorized payment
     * @param record The payment record containing authorization details
     * @return true if capture was successful, false otherwise
     * @throws DomainExceptionPayment if capture fails
     */
    boolean capturePayment(DomainEntityPaymentRecord record);

    /**
     * Voids a previously authorized payment
     * @param record The payment record to void
     * @return true if void was successful, false otherwise
     * @throws DomainExceptionPayment if void fails
     */
    boolean voidPayment(DomainEntityPaymentRecord record);

    /**
     * Refunds a previously captured payment
     * @param record The payment record to refund
     * @param amount The amount to refund
     * @return true if refund was successful, false otherwise
     * @throws DomainExceptionPayment if refund fails
     */
    boolean refundPayment(DomainEntityPaymentRecord record, DomainValueMoney amount);

    /**
     * Verifies the payment method with the gateway
     * @param paymentMethod The payment method to verify
     * @param params Additional verification parameters
     * @return true if payment method is valid, false otherwise
     */
    boolean verifyPaymentMethod(String paymentMethod, Map<String, Object> params);

    /**
     * Retrieves the transaction status from the payment gateway
     * @param transactionId The gateway transaction ID
     * @return The current status from the gateway
     */
    String getTransactionStatus(UUID transactionId);

    /**
     * Checks if the gateway supports a specific payment method
     * @param paymentMethod The payment method to check
     * @return true if the payment method is supported
     */
    boolean supportsPaymentMethod(String paymentMethod);

    /**
     * Gets the gateway's transaction reference for our transaction ID
     * @param transactionId Our internal transaction ID
     * @return The gateway's transaction reference
     */
    String getGatewayReference(UUID transactionId);
}
