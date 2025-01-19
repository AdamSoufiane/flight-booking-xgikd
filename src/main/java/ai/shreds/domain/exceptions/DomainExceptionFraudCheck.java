package ai.shreds.domain.exceptions;

import java.util.UUID;

/**
 * Domain exception for fraud detection.
 * Provides detailed information about detected fraud patterns.
 */
public class DomainExceptionFraudCheck extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String fraudCode;
    private final Integer fraudScore;
    private final String transactionId;
    private final Long userId;

    /**
     * Creates a new fraud check exception
     * @param message The error message
     */
    public DomainExceptionFraudCheck(String message) {
        this(message, null, null, null, null);
    }

    /**
     * Creates a new fraud check exception with a cause
     * @param message The error message
     * @param cause The cause of the error
     */
    public DomainExceptionFraudCheck(String message, Throwable cause) {
        this(message, cause, null, null, null, null);
    }

    /**
     * Creates a new fraud check exception with details
     * @param message The error message
     * @param fraudCode The fraud code
     * @param fraudScore The fraud score
     * @param transactionId The transaction ID
     * @param userId The user ID
     */
    public DomainExceptionFraudCheck(String message, String fraudCode, Integer fraudScore,
                                    String transactionId, Long userId) {
        super(message);
        this.fraudCode = fraudCode;
        this.fraudScore = fraudScore;
        this.transactionId = transactionId;
        this.userId = userId;
    }

    /**
     * Creates a new fraud check exception with all details
     * @param message The error message
     * @param cause The cause of the error
     * @param fraudCode The fraud code
     * @param fraudScore The fraud score
     * @param transactionId The transaction ID
     * @param userId The user ID
     */
    public DomainExceptionFraudCheck(String message, Throwable cause, String fraudCode,
                                    Integer fraudScore, String transactionId, Long userId) {
        super(message, cause);
        this.fraudCode = fraudCode;
        this.fraudScore = fraudScore;
        this.transactionId = transactionId;
        this.userId = userId;
    }

    public String getFraudCode() {
        return fraudCode;
    }

    public Integer getFraudScore() {
        return fraudScore;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Long getUserId() {
        return userId;
    }

    /**
     * Creates an exception for blacklisted user
     * @param userId The blacklisted user ID
     * @return A new fraud check exception
     */
    public static DomainExceptionFraudCheck blacklistedUser(Long userId) {
        return new DomainExceptionFraudCheck(
                String.format("User %d is blacklisted", userId),
                "BLACKLISTED_USER",
                100,
                null,
                userId
        );
    }

    /**
     * Creates an exception for high risk transaction
     * @param transactionId The transaction ID
     * @param fraudScore The fraud score
     * @return A new fraud check exception
     */
    public static DomainExceptionFraudCheck highRiskTransaction(UUID transactionId, int fraudScore) {
        return new DomainExceptionFraudCheck(
                String.format("High risk transaction detected with score: %d", fraudScore),
                "HIGH_RISK_TRANSACTION",
                fraudScore,
                transactionId.toString(),
                null
        );
    }

    /**
     * Creates an exception for suspicious activity
     * @param userId The user ID
     * @param reason The reason for suspicion
     * @return A new fraud check exception
     */
    public static DomainExceptionFraudCheck suspiciousActivity(Long userId, String reason) {
        return new DomainExceptionFraudCheck(
                String.format("Suspicious activity detected: %s", reason),
                "SUSPICIOUS_ACTIVITY",
                null,
                null,
                userId
        );
    }

    /**
     * Creates an exception for exceeded amount limits
     * @param transactionId The transaction ID
     * @param amount The attempted amount
     * @return A new fraud check exception
     */
    public static DomainExceptionFraudCheck exceededLimits(UUID transactionId, String amount) {
        return new DomainExceptionFraudCheck(
                String.format("Transaction amount %s exceeds allowed limits", amount),
                "EXCEEDED_LIMITS",
                null,
                transactionId.toString(),
                null
        );
    }
}
