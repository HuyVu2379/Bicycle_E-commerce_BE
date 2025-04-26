package iuh.paymentservice.services.Impl;

import iuh.paymentservice.dtos.requests.MomoPaymentRequest;
import iuh.paymentservice.dtos.responses.MomoPaymentResponse;
import iuh.paymentservice.entities.Payment;
import iuh.paymentservice.enums.PaymentMethod;
import iuh.paymentservice.enums.PaymentStatus;
import iuh.paymentservice.exception.MessageResponse;
import iuh.paymentservice.repositories.PaymentRepository;
import iuh.paymentservice.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    @Value(value = "${momo.partner-code}")
    private String partnerCode;
    @Value(value = "${momo.access-key}")
    private String accessKey;
    @Value(value = "${momo.secret-key}")
    private String secretKey;
    @Value(value = "${momo.end-point}")
    private String momoEndPoint;

    private final PaymentRepository paymentRepository;


    @Override
    public String processPayment(String userId, double amount, String currency) {
        return "";
    }

    @Override
    public MomoPaymentResponse initiatePayment(MomoPaymentRequest request) {
        if (request.getPaymentMethod() != PaymentMethod.MOMO) {
            throw new IllegalArgumentException("Payment method must be MOMO");
        }
        RestTemplate restTemplate = new RestTemplate();
        String requestId = String.valueOf(System.currentTimeMillis());
        String orderId = String.valueOf(System.currentTimeMillis());
        double amount = request.getAmount();
        String redirectUrl = "http://localhost:8080/api/payment/momo/callback";
        String ipn_url = "http://localhost:8080/api/payment/momo/callback";

        String rawData = String.format(
                "accessKey=%s&amount=%d&extraData=&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=captureWallet",
                accessKey, (long) amount, ipn_url, orderId, "Payment for order " + orderId, partnerCode, redirectUrl, requestId
        );

        String signature = HmacUtils.hmacSha256Hex(secretKey, rawData);

        Map<String, Object> payload = new HashMap<>();
        payload.put("partnerCode", partnerCode);
        payload.put("accessKey", accessKey);
        payload.put("requestId", requestId);
        payload.put("amount", (long) amount);
        payload.put("orderId", orderId);
        payload.put("orderInfo", "Payment for order " + orderId);
        payload.put("redirectUrl", redirectUrl);
        payload.put("ipnUrl", ipn_url);
        payload.put("extraData", "");
        payload.put("requestType", "captureWallet");
        payload.put("signature", signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        Map response = restTemplate.postForObject(momoEndPoint + "/create", entity, Map.class);

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(request.getUserId());
        payment.setAmount(amount);
        payment.setCurrency(request.getCurrency());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return MomoPaymentResponse.builder()
                .paymentUrl((String) response.get("payUrl"))
                .build();
    }

    @Override
    public String handleCallback(String orderId, String resultCode, String signature, String paymentTransactionId) {
        Optional<Payment> payment = paymentRepository.findByOrderId(orderId);
        if (payment.isEmpty()) {
            return "Payment not found";
        }

        String rawData = String.format("orderId=%s&resultCode=%s", orderId, resultCode);
        String expectedSignature = HmacUtils.hmacSha256Hex(secretKey, rawData);
        if (!expectedSignature.equals(signature)) {
            throw new RuntimeException("Invalid signature");
        }

        Payment paymentEntity = payment.get();
        paymentEntity.setPaymentStatus("0".equals(resultCode) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        paymentEntity.setTransactionId(paymentTransactionId);
        paymentEntity.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(paymentEntity);

        if("0".equals(resultCode)) {
            return "Payment successful";
        } else {
            return "Payment failed";
        }
    }
}
