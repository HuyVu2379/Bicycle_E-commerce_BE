package iuh.paymentservice.controller;

import iuh.paymentservice.dtos.requests.PaymentRequest;
import iuh.paymentservice.dtos.responses.PaymentResponse;
import iuh.paymentservice.dtos.responses.VNPayResponse;
import iuh.paymentservice.exception.MessageResponse;
import iuh.paymentservice.services.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<MessageResponse<PaymentResponse>> createPayment(
            @RequestBody PaymentRequest paymentRequest,
            HttpServletRequest request) {

        // Lấy IP address của client
        String ipAddress = request.getRemoteAddr();
        paymentRequest.setIpAddress(ipAddress);

        // Tạo thanh toán
        PaymentResponse paymentResponse = paymentService.createPayment(paymentRequest);

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
        // Lấy tất cả các tham số từ VNPay
        Map<String, String> vnpParams = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();

        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = request.getParameter(paramName);
            if (paramValue != null && paramValue.length() > 0) {
                vnpParams.put(paramName, paramValue);
            }
        }

        // Xử lý callback
        VNPayResponse vnPayResponse = paymentService.processVNPayCallback(vnpParams);

        // Kiểm tra mã phản hồi
        if ("00".equals(vnPayResponse.getVnpResponseCode())) {
            // Thanh toán thành công
            MessageResponse<VNPayResponse> response = new MessageResponse<>(
                    HttpStatus.OK.value(),
                    "Thanh toán thành công",
                    true,
                    vnPayResponse
            );

            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            // Thanh toán thất bại
            MessageResponse<VNPayResponse> response = new MessageResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Thanh toán thất bại: " + vnPayResponse.getVnpResponseCode(),
                    false,
                    vnPayResponse
            );

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{orderId}")
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