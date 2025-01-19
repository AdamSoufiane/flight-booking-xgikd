package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.entities.DomainEntityPaymentRequest;
import ai.shreds.domain.ports.DomainPortPaymentFraud;
import ai.shreds.infrastructure.exceptions.InfrastructureExceptionPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Implementation of the fraud detection port.
 * Delegates fraud checks to external fraud detection service.
 */
@Slf4j
@Service
public class InfrastructurePaymentFraudImpl implements DomainPortPaymentFraud {

    private final InfrastructurePaymentFraudClient paymentFraudClient;

    @Autowired
    public InfrastructurePaymentFraudImpl(InfrastructurePaymentFraudClient paymentFraudClient) {
        this.paymentFraudClient = paymentFraudClient;
    }

    @Override
    public boolean checkFraud(DomainEntityPaymentRequest request) {
        log.debug("Checking fraud for request: {}", request.getId());
        try {
            boolean isFraudulent = paymentFraudClient.verifyRequestForFraud(request);
            log.info("Fraud check result for request {}: {}", request.getId(), isFraudulent);
            return isFraudulent;
        } catch (Exception e) {
            log.error("Failed to check fraud for request: {}", request.getId(), e);
            throw new InfrastructureExceptionPayment("Failed to check fraud", e);
        }
    }

    @Override
    public boolean isUserBlacklisted(Long userId) {
        log.debug("Checking if user is blacklisted: {}", userId);
        try {
            boolean isBlacklisted = paymentFraudClient.checkUserBlacklist(userId);
            log.info("Blacklist check result for user {}: {}", userId, isBlacklisted);
            return isBlacklisted;
        } catch (Exception e) {
            log.error("Failed to check user blacklist: {}", userId, e);
            throw new InfrastructureExceptionPayment("Failed to check user blacklist", e);
        }
    }

    @Override
    public boolean isSuspiciousIP(String ipAddress) {
        log.debug("Checking if IP is suspicious: {}", ipAddress);
        try {
            boolean isSuspicious = paymentFraudClient.checkSuspiciousIP(ipAddress);
            log.info("IP check result for {}: {}", ipAddress, isSuspicious);
            return isSuspicious;
        } catch (Exception e) {
            log.error("Failed to check suspicious IP: {}", ipAddress, e);
            throw new InfrastructureExceptionPayment("Failed to check suspicious IP", e);
        }
    }

    @Override
    public int calculateFraudScore(DomainEntityPaymentRequest request) {
        log.debug("Calculating fraud score for request: {}", request.getId());
        try {
            int score = paymentFraudClient.calculateRiskScore(request);
            log.info("Fraud score for request {}: {}", request.getId(), score);
            return score;
        } catch (Exception e) {
            log.error("Failed to calculate fraud score for request: {}", request.getId(), e);
            throw new InfrastructureExceptionPayment("Failed to calculate fraud score", e);
        }
    }

    @Override
    public boolean isAmountWithinLimits(Long userId, double amount, String currency) {
        log.debug("Checking amount limits for user {}: {} {}", userId, amount, currency);
        try {
            boolean withinLimits = paymentFraudClient.checkTransactionLimits(userId, amount, currency);
            log.info("Amount limit check result for user {}: {}", userId, withinLimits);
            return withinLimits;
        } catch (Exception e) {
            log.error("Failed to check amount limits for user: {}", userId, e);
            throw new InfrastructureExceptionPayment("Failed to check amount limits", e);
        }
    }

    @Override
    public Map<String, Object> getFraudCheckDetails(DomainEntityPaymentRequest request) {
        log.debug("Getting fraud check details for request: {}", request.getId());
        try {
            Map<String, Object> details = paymentFraudClient.getFraudCheckDetails(request);
            log.info("Retrieved fraud check details for request: {}", request.getId());
            return details;
        } catch (Exception e) {
            log.error("Failed to get fraud check details for request: {}", request.getId(), e);
            throw new InfrastructureExceptionPayment("Failed to get fraud check details", e);
        }
    }

    @Override
    public void reportFraudulentTransaction(String transactionId, String reason) {
        log.debug("Reporting fraudulent transaction: {} reason: {}", transactionId, reason);
        try {
            paymentFraudClient.reportFraud(transactionId, reason);
            log.info("Successfully reported fraudulent transaction: {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to report fraudulent transaction: {}", transactionId, e);
            throw new InfrastructureExceptionPayment("Failed to report fraudulent transaction", e);
        }
    }

    @Override
    public boolean updateFraudRules(Map<String, Object> rules) {
        log.debug("Updating fraud rules");
        try {
            boolean updated = paymentFraudClient.updateRules(rules);
            log.info("Fraud rules update result: {}", updated);
            return updated;
        } catch (Exception e) {
            log.error("Failed to update fraud rules", e);
            throw new InfrastructureExceptionPayment("Failed to update fraud rules", e);
        }
    }
}
