package iuh.paymentservice.services.Impl;

import feign.FeignException;
import iuh.paymentservice.clients.FeignClientService;
import iuh.paymentservice.configs.VNPayConfig;
import iuh.paymentservice.dtos.requests.OrderRequest;
import iuh.paymentservice.dtos.requests.PaymentRequest;
import iuh.paymentservice.dtos.responses.*;
import iuh.paymentservice.entities.Payment;
import iuh.paymentservice.enums.OrderStatus;
import iuh.paymentservice.enums.PaymentMethod;
import iuh.paymentservice.enums.PaymentStatus;
import iuh.paymentservice.repositories.PaymentRepository;
import iuh.paymentservice.repositories.PaymentTokenRepository;
import iuh.paymentservice.services.EmailService;
import iuh.paymentservice.services.PaymentService;
import iuh.paymentservice.utils.TemporaryIdGenerator;
import iuh.paymentservice.utils.VNPayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentServiceImpl implements PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private static final Map<String, PaymentStatus> RESPONSE_CODE_TO_STATUS = Map.of(
            "00", PaymentStatus.COMPLETED,
            "01", PaymentStatus.PENDING,
            "02", PaymentStatus.CANCELLED,
            "99", PaymentStatus.FAILED
    );
    private static final Map<String, String> RESPONSE_CODE_TO_ORDER_STATUS = Map.of(
            "00", "COMPLETED",
            "01", "PENDING",
            "02", "CANCELLED",
            "99", "FAILED"
    );

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private VNPayConfig vnPayConfig;
    @Autowired
    private PaymentTokenRepository paymentTokenRepository;
    @Autowired
    private TemporaryIdGenerator temporaryIdGenerator;
    @Autowired
    private FeignClientService feignClientService;
    @Autowired
    private EmailService emailService;

    @Override
    public PaymentResponse createPayment(PaymentRequest paymentRequest, String token) {
        Payment payment = new Payment();
        payment.setOrderId(paymentRequest.getOrderId());
        payment.setUserId(paymentRequest.getUserId());
        payment.setAmount(paymentRequest.getAmount());
        payment.setCurrency(paymentRequest.getCurrency());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);
        String tempId = temporaryIdGenerator.generateTemporaryId();
        String signature = temporaryIdGenerator.generateSignature(payment.getOrderId(), tempId);
        if (token != null && token.startsWith("Bearer ")) {
            paymentTokenRepository.saveToken(tempId, token);
            logger.info("Saved token to Redis for tempId: {}", tempId);
        } else {
            logger.warn("No valid token provided for orderId: {}", paymentRequest.getOrderId());
        }
        String paymentUrl = null;
        if (payment.getPaymentMethod() == PaymentMethod.VN_PAY) {
            paymentUrl = createVNPayPaymentUrl(paymentRequest, tempId, signature);
        }
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .transactionId(payment.getTransactionId())
                .paymentUrl(paymentUrl)
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    private String createVNPayPaymentUrl(PaymentRequest paymentRequest, String tempId, String signature) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = String.format("%s_%s_%s", paymentRequest.getOrderId(), tempId, signature);
        System.out.println("vnp_TxnRef: " + vnp_TxnRef);
        String vnp_IpAddr = paymentRequest.getIpAddress();
        String vnp_TmnCode = vnPayConfig.getTmnCode();
        String orderInfo = paymentRequest.getOrderInfo();
        long amount = (long) (paymentRequest.getAmount() * 100);

        Map<String, String> vnp_Params = new TreeMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", paymentRequest.getCurrency().toString());

        if (paymentRequest.getBankCode() != null && !paymentRequest.getBankCode().isEmpty()) {
            vnp_Params.put("vnp_BankCode", paymentRequest.getBankCode());
        }

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", vnPayConfig.getOrderType());

        String locate = paymentRequest.getLanguage();
        vnp_Params.put("vnp_Locale", (locate != null && !locate.isEmpty()) ? locate : "vn");

        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpnUrl", vnPayConfig.getIpnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8))
                        .append('&');
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
                        .append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8))
                        .append('&');
            }
        }

        if (hashData.length() > 0) {
            hashData.deleteCharAt(hashData.length() - 1);
        }
        if (query.length() > 0) {
            query.deleteCharAt(query.length() - 1);
        }

        String vnp_SecureHash = VNPayUtils.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());

        query.append("&vnp_SecureHash=").append(URLEncoder.encode(vnp_SecureHash, StandardCharsets.UTF_8));

        String paymentUrl = vnPayConfig.getPaymentUrl() + "?" + query.toString();

        logger.info("vnp_TxnRef: {}", vnp_TxnRef);
        logger.info("HashData: {}", hashData.toString());
        logger.info("vnp_SecureHash: {}", vnp_SecureHash);
        logger.info("Payment URL: {}", paymentUrl);

        return paymentUrl;
    }

    @Override
    public VNPayResponse processVNPayCallback(Map<String, String> vnpParams, String token) {
        String vnp_ResponseCode = vnpParams.get("vnp_ResponseCode");
        String vnp_TxnRef = vnpParams.get("vnp_TxnRef");
        String vnp_Amount = vnpParams.get("vnp_Amount");
        String vnp_TransactionNo = vnpParams.get("vnp_TransactionNo");
        String vnp_BankCode = vnpParams.get("vnp_BankCode");
        String vnp_PayDate = vnpParams.get("vnp_PayDate");
        String vnp_OrderInfo = vnpParams.get("vnp_OrderInfo");
        String vnp_SecureHash = vnpParams.get("vnp_SecureHash");

        logger.info("VNPay Callback Params: {}", vnpParams);

        Map<String, String> validationParams = new TreeMap<>();
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            if (!entry.getKey().equals("vnp_SecureHash") && !entry.getKey().equals("vnp_SecureHashType")) {
                validationParams.put(entry.getKey(), entry.getValue());
            }
        }

        String calculatedHash = VNPayUtils.hashAllFields(validationParams, vnPayConfig.getHashSecret());
        logger.info("Calculated Hash: {}", calculatedHash);
        logger.info("Received vnp_SecureHash: {}", vnp_SecureHash);

        String paymentId = null;
        String tempId = null;
        if (calculatedHash.equals(vnp_SecureHash)) {
            String[] txnRefParts = vnp_TxnRef.split("_");
            if (txnRefParts.length < 3) {
                logger.error("Invalid vnp_TxnRef format: {}", vnp_TxnRef);
                return VNPayResponse.builder()
                        .vnpResponseCode(vnp_ResponseCode)
                        .vnpTxnRef(vnp_TxnRef)
                        .vnpAmount(vnp_Amount)
                        .vnpTransactionNo(vnp_TransactionNo)
                        .vnpBankCode(vnp_BankCode)
                        .vnpPayDate(vnp_PayDate)
                        .vnpOrderInfo(vnp_OrderInfo)
                        .paymentId(paymentId)
                        .build();
            }
            String orderId = txnRefParts[0];
            tempId = txnRefParts[1];
            String signature = txnRefParts[2];

            if (!temporaryIdGenerator.verifySignature(orderId, tempId, signature)) {
                logger.error("Invalid signature for vnp_TxnRef: {}", vnp_TxnRef);
                return VNPayResponse.builder()
                        .vnpResponseCode(vnp_ResponseCode)
                        .vnpTxnRef(vnp_TxnRef)
                        .vnpAmount(vnp_Amount)
                        .vnpTransactionNo(vnp_TransactionNo)
                        .vnpBankCode(vnp_BankCode)
                        .vnpPayDate(vnp_PayDate)
                        .vnpOrderInfo(vnp_OrderInfo)
                        .paymentId(paymentId)
                        .build();
            }

            Optional<Payment> paymentOptional = paymentRepository.findByOrderId(orderId);
            if (paymentOptional.isPresent()) {
                Payment payment = paymentOptional.get();
                paymentId = payment.getPaymentId();
                updateRelatedTables(payment, vnp_ResponseCode, vnp_TransactionNo, token, tempId);
            } else {
                logger.error("Payment not found for orderId: {}", orderId);
            }
        } else {
            logger.error("Invalid secure hash for vnp_TxnRef: {}", vnp_TxnRef);
        }

        return VNPayResponse.builder()
                .vnpResponseCode(vnp_ResponseCode)
                .vnpTxnRef(vnp_TxnRef)
                .vnpAmount(vnp_Amount)
                .vnpTransactionNo(vnp_TransactionNo)
                .vnpBankCode(vnp_BankCode)
                .vnpPayDate(vnp_PayDate)
                .vnpOrderInfo(vnp_OrderInfo)
                .paymentId(paymentId)
                .build();
    }

    private void updateRelatedTables(Payment payment, String responseCode, String transactionNo, String token, String tempId) {
        try {
            payment.setPaymentStatus(RESPONSE_CODE_TO_STATUS.getOrDefault(responseCode, PaymentStatus.FAILED));
            payment.setTransactionId(transactionNo);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            OrderStatus orderStatus = OrderStatus.valueOf(RESPONSE_CODE_TO_ORDER_STATUS.getOrDefault(responseCode, "FAILED"));
            OrderRequest orderRequest = OrderRequest.builder()
                    .orderId(payment.getOrderId())
                    .orderStatus(orderStatus)
                    .build();

            String authToken = token;
            try {
                feignClientService.updateOrder(orderRequest, authToken);
                // Gửi email nếu thanh toán thành công
                if (responseCode.equals("00")) {
                    sendPaymentSuccessEmail(payment, token);
                }
            } catch (FeignException e) {
                logger.error("Feign error updating order for paymentId: {}. Status: {}, Message: {}",
                        payment.getPaymentId(), e.status(), e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Unexpected error updating related tables for paymentId: {}", payment.getPaymentId(), e);
        }
    }

    @Async
    protected void sendPaymentSuccessEmail(Payment payment, String token) {
        try {
            // Lấy email người dùng
            String userEmail = feignClientService.getEmailUser(payment.getUserId(), token).getData();
            if (userEmail == null || userEmail.isEmpty()) {
                logger.error("No email found for userId: {}", payment.getUserId());
                return;
            }

            // Lấy chi tiết đơn hàng
            OrderResponse orderResponse = feignClientService.getOrderById(payment.getOrderId(), token).getData();
            if (orderResponse == null) {
                logger.error("No order details found for orderId: {}", payment.getOrderId());
                return;
            }
            List<OrderDetailResponse> orderDetails = orderResponse.getOrderDetails();

            // Định dạng tiền tệ
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String formattedAmount = currencyFormat.format(payment.getAmount());

            // Tạo bảng sản phẩm
            StringBuilder itemsTable = new StringBuilder();
            itemsTable.append("<table style='border-collapse: collapse; width: 100%; max-width: 600px; font-family: Arial, sans-serif; font-size: 14px; margin: 20px 0;'>");
            itemsTable.append("<thead style='background-color: #4CAF50; color: white;'>");
            itemsTable.append("<tr>");
            itemsTable.append("<th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>Sản phẩm</th>");
            itemsTable.append("<th style='padding: 12px; text-align: center; border: 1px solid #ddd;'>Số lượng</th>");
            itemsTable.append("<th style='padding: 12px; text-align: center; border: 1px solid #ddd;'>Màu sắc</th>");
            itemsTable.append("<th style='padding: 12px; text-align: right; border: 1px solid #ddd;'>Giá</th>");
            itemsTable.append("</tr>");
            itemsTable.append("</thead>");
            itemsTable.append("<tbody>");
            for (OrderDetailResponse item : orderDetails) {
                String nameProduct = feignClientService.getProductName(item.getProductId()).getData();
                if (nameProduct == null || nameProduct.isEmpty()) {
                    nameProduct = "Sản phẩm không xác định (ID: " + item.getProductId() + ")";
                }
                String formattedSubtotal = currencyFormat.format(item.getSubtotal());
                itemsTable.append("<tr>");
                itemsTable.append("<td style='padding: 12px; border: 1px solid #ddd;'>").append(nameProduct).append("</td>");
                itemsTable.append("<td style='padding: 12px; text-align: center; border: 1px solid #ddd;'>").append(item.getQuantity()).append("</td>");
                itemsTable.append("<td style='padding: 12px; text-align: center; border: 1px solid #ddd;'>").append(item.getColor() != null ? item.getColor() : "N/A").append("</td>");
                itemsTable.append("<td style='padding: 12px; text-align: right; border: 1px solid #ddd;'>").append(formattedSubtotal).append("</td>");
                itemsTable.append("</tr>");
            }
            itemsTable.append("</tbody>");
            itemsTable.append("</table>");

            // Lấy địa chỉ
            AddressResponse addressResponse = feignClientService.getAddressByUserId(payment.getUserId(), token).getData();
            String fullAddress = (addressResponse != null && addressResponse.getFullAddress() != null) ? addressResponse.getFullAddress() : "N/A";

            // Tạo nội dung email HTML
            String emailBody = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<meta charset='UTF-8'>" +
                    "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "<title>Thanh toán thành công</title>" +
                    "</head>" +
                    "<body style='font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                    "<div style='text-align: center; margin-bottom: 20px;'>" +
                    "<img src='https://media.istockphoto.com/id/854733622/vector/bicycle-icon.jpg?s=612x612&w=0&k=20&c=cu34k4KEV5VYWwwVbMAmPogLJmh-OBITXEd1d9rWfrw=' alt='Shop Logo' style='max-width: 150px;'>" +
                    "</div>" +
                    "<h2 style='color: #4CAF50; text-align: center;'>Chúc mừng bạn đã thanh toán thành công!</h2>" +
                    "<p style='text-align: center;'>Cảm ơn bạn đã tin tưởng và mua sắm tại <strong>Web site bicycle-E-commerce</strong>.</p>" +
                    "<div style='background-color: #f9f9f9; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
                    "<h3 style='color: #333; margin-top: 0;'>Thông tin đơn hàng</h3>" +
                    "<p><strong>Mã đơn hàng:</strong> " + payment.getOrderId() + "</p>" +
                    "<p><strong>Tổng tiền:</strong> " + formattedAmount + "</p>" +
                    "<p><strong>Địa chỉ nhận hàng:</strong> " + fullAddress + "</p>" +
                    "</div>" +
                    "<h3 style='color: #333;'>Chi tiết sản phẩm</h3>" +
                    itemsTable.toString() +
                    "<p>Đơn hàng của bạn sẽ được xử lý và giao đến trong thời gian sớm nhất. Nếu có bất kỳ câu hỏi nào, vui lòng liên hệ qua email <a href='huyvu2379kh@gmail.com'>huyvu2379kh@gmail.com</a>.</p>" +
                    "<p style='text-align: center; margin-top: 30px;'>Trân trọng,<br><strong>Web site bicycle-E-commerce</strong></p>" +
                    "<div style='text-align: center; margin-top: 20px; font-size: 12px; color: #777;'>" +
                    "<p>12 Nguyễn Văn Bảo, phường 4, Gò Vấp, thành phố Hồ Chí Minh</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            emailService.sendEmail(userEmail, "Thanh toán thành công - Đơn hàng " + payment.getOrderId(), emailBody);
            logger.info("Sent payment success email for paymentId: {}", payment.getPaymentId());
        } catch (Exception e) {
            logger.error("Failed to send payment success email for paymentId: {}", payment.getPaymentId(), e);
        }
    }

    @Override
    public PaymentResponse getPaymentByOrderId(String orderId) {
        Optional<Payment> paymentOptional = paymentRepository.findByOrderId(orderId);

        if (paymentOptional.isPresent()) {
            Payment payment = paymentOptional.get();
            return PaymentResponse.builder()
                    .paymentId(payment.getPaymentId())
                    .orderId(payment.getOrderId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .paymentMethod(payment.getPaymentMethod())
                    .paymentStatus(payment.getPaymentStatus())
                    .transactionId(payment.getTransactionId())
                    .createdAt(payment.getCreatedAt())
                    .updatedAt(payment.getUpdatedAt())
                    .build();
        }
        return null;
    }

    @Override
    public Optional<Payment> getPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId);
    }
}