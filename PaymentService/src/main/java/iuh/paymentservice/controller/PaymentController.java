package iuh.paymentservice.controller;

import iuh.paymentservice.clients.FeignClientService;
import iuh.paymentservice.dtos.requests.OrderRequest;
import iuh.paymentservice.dtos.requests.PaymentRequest;
import iuh.paymentservice.dtos.responses.PaymentResponse;
import iuh.paymentservice.dtos.responses.VNPayResponse;
import iuh.paymentservice.entities.Payment;
import iuh.paymentservice.enums.PaymentStatus;
import iuh.paymentservice.exception.MessageResponse;
import iuh.paymentservice.repositories.PaymentRepository;
import iuh.paymentservice.repositories.PaymentTokenRepository;
import iuh.paymentservice.services.Impl.PaymentServiceImpl;
import iuh.paymentservice.services.PaymentService;
import iuh.paymentservice.utils.TemporaryIdGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;
    private final PaymentTokenRepository paymentTokenRepository;
    private final TemporaryIdGenerator temporaryIdGenerator;

    @Autowired
    public PaymentController(
            PaymentService paymentService,
            PaymentTokenRepository paymentTokenRepository,
            TemporaryIdGenerator temporaryIdGenerator) {
        this.paymentService = paymentService;
        this.paymentTokenRepository = paymentTokenRepository;
        this.temporaryIdGenerator = temporaryIdGenerator;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MessageResponse<PaymentResponse>> createPayment(
            @RequestBody PaymentRequest paymentRequest,
            HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        paymentRequest.setIpAddress(ipAddress);
        String token = request.getHeader("Authorization");
        PaymentResponse paymentResponse = paymentService.createPayment(paymentRequest, token);
        MessageResponse<PaymentResponse> response = new MessageResponse<>(
                HttpStatus.CREATED.value(),
                "Tạo thanh toán thành công",
                true,
                paymentResponse
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/vnpay-callback")
    public ResponseEntity<MessageResponse<VNPayResponse>> vnpayCallback(HttpServletRequest request) {
        Map<String, String> vnpParams = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            if (paramValue != null && !paramValue.isEmpty()) {
                vnpParams.put(paramName, paramValue);
            }
        }

        String vnp_TxnRef = vnpParams.get("vnp_TxnRef");
        String[] parts = vnp_TxnRef.split("_");
        if (parts.length < 3) {
            logger.error("Invalid vnp_TxnRef format: {}", vnp_TxnRef);
            MessageResponse<VNPayResponse> response = new MessageResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Invalid vnp_TxnRef format",
                    false,
                    null
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        String orderId = parts[0];
        String tempId = parts[1];
        String signature = parts[2];

        // Xác minh signature
        if (!temporaryIdGenerator.verifySignature(orderId, tempId, signature)) {
            logger.error("Invalid signature for vnp_TxnRef: {}", vnp_TxnRef);
            MessageResponse<VNPayResponse> response = new MessageResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Invalid signature",
                    false,
                    null
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Lấy token từ Redis
        String token = paymentTokenRepository.getToken(tempId);
        if (token == null) {
            logger.warn("No token found for tempId: {}", tempId);
        }

        // Gọi service với token
        VNPayResponse vnPayResponse = paymentService.processVNPayCallback(vnpParams, token);
        MessageResponse<VNPayResponse> response = new MessageResponse<>(
                vnPayResponse.getVnpResponseCode().equals("00") ? HttpStatus.OK.value() : HttpStatus.BAD_REQUEST.value(),
                vnPayResponse.getVnpResponseCode().equals("00") ? "Thanh toán thành công" : "Thanh toán thất bại: " + vnPayResponse.getVnpResponseCode(),
                vnPayResponse.getVnpResponseCode().equals("00"),
                vnPayResponse
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<MessageResponse<PaymentResponse>> getPaymentByOrderId(@PathVariable String orderId) {
        PaymentResponse paymentResponse = paymentService.getPaymentByOrderId(orderId);
        if (paymentResponse != null) {
            MessageResponse<PaymentResponse> response = new MessageResponse<>(
                    HttpStatus.OK.value(),
                    "Lấy thông tin thanh toán thành công",
                    true,
                    paymentResponse
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            MessageResponse<PaymentResponse> response = new MessageResponse<>(
                    HttpStatus.NOT_FOUND.value(),
                    "Không tìm thấy thanh toán với orderId: " + orderId,
                    false,
                    null
            );
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }
}