package payment_service.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import payment_service.dto.PaymentRequest;
import payment_service.dto.PaymentResponse;
import payment_service.model.Payment;
import payment_service.model.PaymentStatus;
import payment_service.security.ApiKeyFilter;
import payment_service.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private ApiKeyFilter apiKeyFilter;

    @BeforeEach
    public void setup() {
        apiKeyFilter = new ApiKeyFilter();
        ReflectionTestUtils.setField(apiKeyFilter, "expectedApiKey", "wisdom-payment-key-2026");

        mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
                .addFilters(apiKeyFilter)
                .build();
    }

    @Test
    public void testProcessPaymentUnauthorized() throws Exception {
        String requestJson = "{\"userId\": 1, \"orderId\": 101, \"amount\": 150.00, \"paymentMethod\": \"CREDIT_CARD\"}";

        // Request without API Key should return 401 Unauthorized
        mockMvc.perform(post("/payments/process")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testProcessPaymentSuccess() throws Exception {
        Payment mockPayment = new Payment(101L, 1L, new BigDecimal("150.00"), "CREDIT_CARD", PaymentStatus.COMPLETED, "TXN-12345", LocalDateTime.now());
        mockPayment.setId("99");
        PaymentResponse response = new PaymentResponse(mockPayment);

        when(paymentService.createPayment(any(PaymentRequest.class))).thenReturn(response);

        String requestJson = "{\"userId\": 1, \"orderId\": 101, \"amount\": 150.00, \"paymentMethod\": \"CREDIT_CARD\"}";

        mockMvc.perform(post("/payments/process")
                .header("X-API-KEY", "wisdom-payment-key-2026")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("99"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.orderId").value(101))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.transactionReference").value("TXN-12345"));
    }

    @Test
    public void testGetPaymentHistorySuccess() throws Exception {
        when(paymentService.getAllPayments(any())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/payments/history")
                .header("X-API-KEY", "wisdom-payment-key-2026"))
                .andExpect(status().isOk());
    }
}
