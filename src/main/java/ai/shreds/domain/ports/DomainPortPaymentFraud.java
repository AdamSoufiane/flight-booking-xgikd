package ai.shreds.domain.ports;

import ai.shreds.domain.entities.DomainEntityPaymentRequest;
import ai.shreds.domain.exceptions.DomainExceptionFraudCheck;

import java.util.Map;

/**
 * Port for fraud detection services.
 * Defines the contract for fraud detection operations in the payment process.
 */
public interface DomainPortPaymentFraud {

    /**
     * Performs a comprehensive fraud check on a payment request
     * @param request The payment request to check
     * @return true if the request is legitimate, false if fraud is detected
     * @throws DomainExceptionFraudCheck if fraud check fails or fraud is detected
     */
    boolean checkFraud(DomainEntityPaymentRequest request);

    /**
     * Checks if a user is blacklisted
     * @param userId The user ID to check
     * @return true if user is blacklisted
     */
    boolean isUserBlacklisted(Long userId);

    /**
     * Checks if an IP address is suspicious
     * @param ipAddress The IP address to check
     * @return true if IP is suspicious
     */
    boolean isSuspiciousIP(String ipAddress);

    /**
     * Calculates a fraud score for a payment request
     * @param request The payment request to score
     * @return A score between 0 (safe) and 100 (high risk)
     */
    int calculateFraudScore(DomainEntityPaymentRequest request);

    /**
     * Verifies if the payment amount is within normal limits for the user
     * @param userId The user ID
     * @param amount The payment amount
     * @param currency The payment currency
     * @return true if the amount is within normal limits
     */
    boolean isAmountWithinLimits(Long userId, double amount, String currency);

    /**
     * Gets the fraud check details for audit purposes
     * @param request The payment request
     * @return Map containing fraud check details
     */
    Map<String, Object> getFraudCheckDetails(DomainEntityPaymentRequest request);

    /**
     * Reports a confirmed fraudulent transaction
     * @param transactionId The ID of the fraudulent transaction
     * @param reason The reason for reporting fraud
     */
    void reportFraudulentTransaction(String transactionId, String reason);

    /**
     * Updates the fraud detection rules
     * @param rules Map of rule names to rule configurations
     * @return true if rules were successfully updated
     */
    boolean updateFraudRules(Map<String, Object> rules);
}
