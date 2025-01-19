package ai.shreds.domain.services;

import ai.shreds.domain.ports.DomainPortPaymentFraud;
import ai.shreds.domain.entities.DomainEntityPaymentRequest;
import ai.shreds.domain.exceptions.DomainExceptionFraudCheck;
import ai.shreds.domain.value_objects.DomainValueMoney;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Domain service for fraud detection and prevention.
 * Coordinates various fraud checks and risk assessments.
 */
@Slf4j
@Service
public class DomainServiceFraudCheck {

    private static final BigDecimal HIGH_RISK_AMOUNT = new BigDecimal("10000.00");
    private static final int HIGH_RISK_SCORE_THRESHOLD = 80;
    private static final int MAX_ATTEMPTS_PER_USER = 3;

    private final DomainPortPaymentFraud fraudPort;

    public DomainServiceFraudCheck(DomainPortPaymentFraud fraudPort) {
        this.fraudPort = fraudPort;
    }

    /**
     * Performs comprehensive fraud check for a payment request
     * @param request The payment request to check
     * @return true if fraud is detected
     * @throws DomainExceptionFraudCheck if fraud check fails
     */
    public boolean performFraudCheck(DomainEntityPaymentRequest request) {
        log.info("Performing fraud check for user: {}", request.getUserId());
        try {
            // Check if user is blacklisted
            if (fraudPort.isUserBlacklisted(request.getUserId())) {
                log.warn("User {} is blacklisted", request.getUserId());
                throw new DomainExceptionFraudCheck("User is blacklisted");
            }

            // Get fraud score
            int fraudScore = fraudPort.calculateFraudScore(request);
            log.debug("Fraud score for request {}: {}", request.getId(), fraudScore);

            // Check high-risk transactions
            if (isHighRiskTransaction(request, fraudScore)) {
                log.warn("High risk transaction detected for user: {}", request.getUserId());
                return true;
            }

            // Check amount limits
            DomainValueMoney money = request.getMoney();
            if (!fraudPort.isAmountWithinLimits(request.getUserId(), 
                    money.getAmount().doubleValue(), 
                    money.getCurrency())) {
                log.warn("Amount exceeds limits for user: {}", request.getUserId());
                return true;
            }

            // Get detailed fraud check results
            Map<String, Object> fraudDetails = fraudPort.getFraudCheckDetails(request);
            logFraudCheckDetails(request, fraudDetails);

            // Perform final fraud check
            boolean isFraudulent = fraudPort.checkFraud(request);
            if (isFraudulent) {
                log.warn("Fraud detected for request: {}", request.getId());
                fraudPort.reportFraudulentTransaction(
                        request.getId().toString(),
                        "Fraud detected during standard checks");
            }

            return isFraudulent;

        } catch (DomainExceptionFraudCheck e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during fraud check for user: {}", request.getUserId(), e);
            throw new DomainExceptionFraudCheck("Fraud check failed", e);
        }
    }

    /**
     * Determines if a transaction is high risk
     * @param request The payment request
     * @param fraudScore The calculated fraud score
     * @return true if the transaction is high risk
     */
    private boolean isHighRiskTransaction(DomainEntityPaymentRequest request, int fraudScore) {
        // Check if amount exceeds high-risk threshold
        boolean isHighAmount = request.getAmount().compareTo(HIGH_RISK_AMOUNT) >= 0;

        // Check if fraud score exceeds threshold
        boolean isHighScore = fraudScore >= HIGH_RISK_SCORE_THRESHOLD;

        return isHighAmount || isHighScore;
    }

    /**
     * Logs fraud check details
     * @param request The payment request
     * @param fraudDetails The fraud check details
     */
    private void logFraudCheckDetails(DomainEntityPaymentRequest request, Map<String, Object> fraudDetails) {
        if (log.isDebugEnabled()) {
            log.debug("Fraud check details for request {}: {}", request.getId(), fraudDetails);
        }
    }
}
