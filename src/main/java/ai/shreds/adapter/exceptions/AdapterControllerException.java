package ai.shreds.adapter.exceptions;

import ai.shreds.application.exceptions.ApplicationException;
import ai.shreds.domain.exceptions.DomainException;
import ai.shreds.shared.dtos.SharedErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * Global exception handler for the API layer.
 * Handles various types of exceptions and converts them to appropriate HTTP responses.
 */
@ControllerAdvice
public class AdapterControllerException {

    /**
     * Handles validation exceptions from request body validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        SharedErrorResponseDTO errorResponse = SharedErrorResponseDTO.of(
                "Validation failed",
                "VALIDATION_ERROR",
                errors
        );
        errorResponse.setPath(request.getDescription(false));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handles validation exceptions from @Validated on controller level.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        String errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));

        SharedErrorResponseDTO errorResponse = SharedErrorResponseDTO.of(
                "Validation failed",
                "CONSTRAINT_VIOLATION",
                errors
        );
        errorResponse.setPath(request.getDescription(false));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handles application layer exceptions.
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleApplicationException(
            ApplicationException ex, WebRequest request) {
        SharedErrorResponseDTO errorResponse = SharedErrorResponseDTO.of(
                ex.getMessage(),
                "APPLICATION_ERROR",
                ex.getDetails()
        );
        errorResponse.setPath(request.getDescription(false));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handles domain layer exceptions.
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<SharedErrorResponseDTO> handleDomainException(
            DomainException ex, WebRequest request) {
        SharedErrorResponseDTO errorResponse = SharedErrorResponseDTO.of(
                ex.getMessage(),
                "DOMAIN_ERROR",
                ex.getDetails()
        );
        errorResponse.setPath(request.getDescription(false));

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    /**
     * Handles all other unhandled exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<SharedErrorResponseDTO> handleGenericException(
            Exception ex, WebRequest request) {
        SharedErrorResponseDTO errorResponse = SharedErrorResponseDTO.of(
                "An unexpected error occurred",
                "INTERNAL_SERVER_ERROR",
                ex.getMessage()
        );
        errorResponse.setPath(request.getDescription(false));

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}