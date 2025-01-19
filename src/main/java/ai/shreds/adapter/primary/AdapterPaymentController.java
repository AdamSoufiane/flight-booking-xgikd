package ai.shreds.adapter.primary;

import ai.shreds.application.ports.ApplicationPaymentInputPort;
import ai.shreds.shared.SharedPaymentRequestParams;
import ai.shreds.shared.SharedPaymentResponseDTO;
import ai.shreds.shared.SharedPaymentStatusResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for handling payment-related operations.
 * Provides endpoints for creating payments and checking payment status.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment Operations", description = "APIs for payment processing and status checking")
public class AdapterPaymentController {

    private final ApplicationPaymentInputPort applicationPaymentInputPort;

    @Autowired
    public AdapterPaymentController(ApplicationPaymentInputPort applicationPaymentInputPort) {
        this.applicationPaymentInputPort = applicationPaymentInputPort;
    }

    /**
     * Creates a new payment transaction
     *
     * @param params Payment request parameters
     * @return Payment response with transaction details
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new payment transaction",
            description = "Processes a payment request and returns transaction details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment created successfully",
                    content = @Content(schema = @Schema(implementation = SharedPaymentResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "403", description = "Fraud detection alert"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SharedPaymentResponseDTO> createPayment(
            @Valid @RequestBody SharedPaymentRequestParams params) {
        log.info("Received payment request for user: {}", params.getUserId());
        SharedPaymentResponseDTO response = applicationPaymentInputPort.createPayment(params);
        log.info("Payment processed for transaction: {}", response.getTransactionId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves the current status of a payment transaction
     *
     * @param transactionId UUID of the transaction
     * @return Current payment status
     */
    @GetMapping(value = "/{transactionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get payment transaction status",
            description = "Retrieves the current status of a payment transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SharedPaymentStatusResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Transaction not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SharedPaymentStatusResponseDTO> getPaymentStatus(
            @Parameter(description = "Transaction ID", required = true)
            @PathVariable UUID transactionId) {
        log.info("Retrieving status for transaction: {}", transactionId);
        SharedPaymentStatusResponseDTO statusDTO = applicationPaymentInputPort.getPaymentStatus(transactionId);
        log.info("Status retrieved for transaction: {}, status: {}", transactionId, statusDTO.getStatus());
        return ResponseEntity.ok(statusDTO);
    }
}
