package payment_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import payment_service.dto.PaymentRequest;
import payment_service.dto.PaymentResponse;
import payment_service.exception.ResourceNotFoundException;
import payment_service.model.Payment;
import payment_service.model.PaymentStatus;
import payment_service.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentResponse createPayment(PaymentRequest request) {
        // Mock payment gateway approval directly as COMPLETED
        PaymentStatus initialStatus = PaymentStatus.COMPLETED;

        // Generate a random unique transaction reference
        String transactionReference = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = new Payment(
                request.getOrderId(),
                request.getUserId(),
                request.getAmount(),
                request.getPaymentMethod(),
                initialStatus,
                transactionReference,
                LocalDateTime.now()
        );

        Payment savedPayment = paymentRepository.save(payment);
        return new PaymentResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(String id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        return new PaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments(Long userId) {
        List<Payment> payments;
        if (userId != null) {
            payments = paymentRepository.findByUserId(userId);
        } else {
            payments = paymentRepository.findAll();
        }
        return payments.stream()
                .map(PaymentResponse::new)
                .collect(Collectors.toList());
    }

    public PaymentResponse updatePaymentStatus(String id, String statusStr) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));

        PaymentStatus newStatus;
        try {
            newStatus = PaymentStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + statusStr + ". Must be PENDING, COMPLETED, or FAILED.");
        }

        payment.setStatus(newStatus);
        Payment updatedPayment = paymentRepository.save(payment);
        return new PaymentResponse(updatedPayment);
    }
}
