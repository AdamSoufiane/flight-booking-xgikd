package ai.shreds.adapter.exceptions;

import ai.shreds.shared.SharedErrorResponseDTO;
import ai.shreds.application.exceptions.ApplicationPaymentException;
import ai.shreds.domain.exceptions.DomainExceptionFraudCheck;
import ai.shreds.domain.exceptions.DomainExceptionPayment;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Global exception handler for payment-related exceptions.
 * Provides centralized exception handling across all payment endpoints.
 */
@Slf4j
@RestControllerAdvice
public class AdapterPaymentExceptionHandler {

    /**
     * Handles application-level payment exceptions
     * @param ex The caught ApplicationPaymentException
     * @return Error response with appropriate HTTP status
     */
    @ExceptionHandler(ApplicationPaymentException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<SharedErrorResponseDTO> handlePaymentException(ApplicationPaymentException ex) {
        log.error("Payment processing error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                SharedErrorResponseDTO.error(ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /**
     * Handles domain-level fraud check exceptions
     * @param ex The caught DomainExceptionFraudCheck
     * @return Error response with fraud alert and FORBIDDEN status
     */
    @ExceptionHandler(DomainExceptionFraudCheck.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<SharedErrorResponseDTO> handleFraudException(DomainExceptionFraudCheck ex) {
        log.warn("Fraud detection alert: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                SharedErrorResponseDTO.fraudAlert(ex.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }

    /**
     * Handles domain-level payment exceptions
     * @param ex The caught DomainExceptionPayment
     * @return Error response with appropriate HTTP status
     */
    @ExceptionHandler(DomainExceptionPayment.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<SharedErrorResponseDTO> handleDomainPaymentException(DomainExceptionPayment ex) {
        log.error("Domain payment error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                SharedErrorResponseDTO.error(ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handles validation exceptions from @Valid annotations
     * @param ex The caught MethodArgumentNotValidException
     * @return Error response with validation details
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<SharedErrorResponseDTO> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        log.warn("Validation error: {}", errorMessage);
        return new ResponseEntity<>(
                SharedErrorResponseDTO.error("Validation failed: " + errorMessage),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handles constraint violation exceptions
     * @param ex The caught ConstraintViolationException
     * @return Error response with constraint violation details
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<SharedErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        log.warn("Constraint violation: {}", errorMessage);
        return new ResponseEntity<>(
                SharedErrorResponseDTO.error("Validation failed: " + errorMessage),
                HttpStatus.BAD_REQUEST
        );
    }

    /**
     * Handles all other unhandled exceptions
     * @param ex The caught Exception
     * @return Generic error response
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<SharedErrorResponseDTO> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                SharedErrorResponseDTO.error("An unexpected error occurred. Please try again later."),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
