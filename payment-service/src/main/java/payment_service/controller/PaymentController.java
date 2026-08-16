package payment_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment_service.dto.PaymentRequest;
import payment_service.dto.PaymentResponse;
import payment_service.dto.StatusUpdateRequest;
import payment_service.service.PaymentService;

import java.util.List;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payment Management API", description = "Endpoints for processing and managing LMS student payments")
@SecurityRequirement(name = "ApiKeyAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    @Operation(summary = "Process a new payment", description = "Creates a payment record for a student purchasing a course/order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request input parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid API Key")
    })
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/history")
    @Operation(summary = "Get payment history", description = "Retrieves all payment transactions or filters by a specific student (userId)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of payments retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid API Key")
    })
    public ResponseEntity<List<PaymentResponse>> getPaymentHistory(
            @Parameter(description = "Optional ID of the user to filter payments") 
            @RequestParam(required = false) Long userId) {
        List<PaymentResponse> history = paymentService.getAllPayments(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieves detailed information of a specific payment by its database ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment record found"),
            @ApiResponse(responseCode = "404", description = "Payment record not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid API Key")
    })
    public ResponseEntity<PaymentResponse> getPaymentById(
            @Parameter(description = "Database ID of the payment to retrieve", required = true) 
            @PathVariable String id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update payment status", description = "Updates the status of an existing payment (COMPLETED, PENDING, or FAILED)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status value provided"),
            @ApiResponse(responseCode = "404", description = "Payment record not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid API Key")
    })
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @Parameter(description = "Database ID of the payment to update", required = true) 
            @PathVariable String id,
            @Valid @RequestBody StatusUpdateRequest request) {
        PaymentResponse response = paymentService.updatePaymentStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }
}
