/*
 * @ (#) MomoApi.java       1.0     4/25/2025
 *
 * Copyright (c) 2025. All rights reserved.
 */

package iuh.paymentservice.controller;
/*
 * @author: Luong Tan Dat
 * @date: 4/25/2025
 */

import iuh.paymentservice.dtos.requests.MomoPaymentRequest;
import iuh.paymentservice.dtos.responses.MomoPaymentResponse;
import iuh.paymentservice.exception.MessageResponse;
import iuh.paymentservice.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
//@Slf4j(topic = "MOMO-PAYMENT-CONTROLLER")
public class MomoPaymentController {
    private final PaymentService paymentService;

//    @PostMapping("/momo")
//    MessageResponse<MomoPaymentResponse> createMomoQR(@RequestBody MomoPaymentRequest createMomoRequest, @RequestHeader("X-Gateway-Source") String source) {
//        if (!"trusted-gateway".equals(source)) {
//            log.info("Invalid source: {}", source);
//            throw new RuntimeException("Invalid source");
//        }
//        return new MessageResponse<>(200, "Success", true, paymentService.initiatePayment(createMomoRequest));
//    }
//
//    @GetMapping("/momo/callback")
//    public MessageResponse<String> handleMomoCallback(@RequestParam String orderId,
//                                                      @RequestParam String resultCode,
//                                                      @RequestParam String signature,
//                                                      @RequestParam String transactionId) {
//        log.info("Momo callback received: orderId={}, resultCode={}, signature={}, transactionId={}", orderId, resultCode, signature, transactionId);
//        return new MessageResponse<>(200, "Callback handled successfully", true, paymentService.handleCallback("orderId", resultCode, signature, transactionId));
//    }
}
