package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.entities.DomainEntityPaymentRequest;
import ai.shreds.infrastructure.exceptions.InfrastructureExceptionPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client for external fraud detection service.
 * Currently implements mock behavior for testing purposes.
 */
@Slf4j
@Component
public class InfrastructurePaymentFraudClient {

    private static final int HIGH_RISK_SCORE = 80;
    private static final double DEFAULT_TRANSACTION_LIMIT = 10000.0;

    // Mock storage for blacklisted users and suspicious IPs
    private final Set<Long> blacklistedUsers = ConcurrentHashMap.newKeySet();
    private final Set<String> suspiciousIPs = ConcurrentHashMap.newKeySet();
    private final Map<String, Object> fraudRules = new ConcurrentHashMap<>();

    /**
     * Verifies a payment request for fraud
     * @param request The payment request to verify
     * @return true if fraud is detected
     */
    public boolean verifyRequestForFraud(DomainEntityPaymentRequest request) {
        log.debug("Verifying request for fraud: {}", request.getId());
        try {
            // Simulate processing time
            Thread.sleep(100);

            // Check if user is blacklisted
            if (blacklistedUsers.contains(request.getUserId())) {
                return true;
            }

            // Calculate risk score
            int score = calculateRiskScore(request);
            return score >= HIGH_RISK_SCORE;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw handleFraudCheckError(e);
        }
    }

    /**
     * Checks if a user is blacklisted
     * @param userId The user ID to check
     * @return true if user is blacklisted
     */
    public boolean checkUserBlacklist(Long userId) {
        log.debug("Checking user blacklist: {}", userId);
        return blacklistedUsers.contains(userId);
    }

    /**
     * Checks if an IP address is suspicious
     * @param ipAddress The IP address to check
     * @return true if IP is suspicious
     */
    public boolean checkSuspiciousIP(String ipAddress) {
        log.debug("Checking suspicious IP: {}", ipAddress);
        return suspiciousIPs.contains(ipAddress);
    }

    /**
     * Calculates a risk score for a payment request
     * @param request The payment request to score
     * @return Risk score between 0 and 100
     */
    public int calculateRiskScore(DomainEntityPaymentRequest request) {
        log.debug("Calculating risk score for request: {}", request.getId());
        try {
            // Simulate processing time
            Thread.sleep(50);

            // Mock risk calculation based on amount
            double amount = request.getAmount().doubleValue();
            if (amount > DEFAULT_TRANSACTION_LIMIT) {
                return HIGH_RISK_SCORE;
            }
            return (int) ((amount / DEFAULT_TRANSACTION_LIMIT) * 70);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw handleFraudCheckError(e);
        }
    }

    /**
     * Checks if a transaction amount is within limits
     * @param userId The user ID
     * @param amount The transaction amount
     * @param currency The transaction currency
     * @return true if amount is within limits
     */
    public boolean checkTransactionLimits(Long userId, double amount, String currency) {
        log.debug("Checking transaction limits for user {}: {} {}", userId, amount, currency);
        // Mock implementation - simple limit check
        return amount <= DEFAULT_TRANSACTION_LIMIT;
    }

    /**
     * Gets fraud check details for a request
     * @param request The payment request
     * @return Map of fraud check details
     */
    public Map<String, Object> getFraudCheckDetails(DomainEntityPaymentRequest request) {
        log.debug("Getting fraud check details for request: {}", request.getId());
        Map<String, Object> details = new HashMap<>();
        details.put("risk_score", calculateRiskScore(request));
        details.put("user_blacklisted", blacklistedUsers.contains(request.getUserId()));
        details.put("transaction_limit", DEFAULT_TRANSACTION_LIMIT);
        return details;
    }

    /**
     * Reports a fraudulent transaction
     * @param transactionId The transaction ID
     * @param reason The reason for reporting
     */
    public void reportFraud(String transactionId, String reason) {
        log.debug("Reporting fraud for transaction: {} reason: {}", transactionId, reason);
        // In a real implementation, this would call an external fraud reporting service
        log.info("Fraud reported for transaction: {}", transactionId);
    }

    /**
     * Updates fraud detection rules
     * @param rules The new rules to apply
     * @return true if rules were updated successfully
     */
    public boolean updateRules(Map<String, Object> rules) {
        log.debug("Updating fraud rules");
        try {
            fraudRules.clear();
            fraudRules.putAll(rules);
            return true;
        } catch (Exception e) {
            log.error("Failed to update fraud rules", e);
            return false;
        }
    }

    /**
     * Handles fraud check errors
     * @param error The error to handle
     * @return InfrastructureExceptionPayment
     */
    private InfrastructureExceptionPayment handleFraudCheckError(Exception error) {
        log.error("Fraud check error occurred", error);
        return new InfrastructureExceptionPayment("Fraud check error: " + error.getMessage(), error);
    }
}
