package iuh.paymentservice.services.Impl;

import iuh.paymentservice.configs.VNPayConfig;
import iuh.paymentservice.dtos.requests.PaymentRequest;
import iuh.paymentservice.dtos.requests.VNPayRequest;
import iuh.paymentservice.dtos.responses.PaymentResponse;
import iuh.paymentservice.dtos.responses.VNPayResponse;
import iuh.paymentservice.entities.Payment;
import iuh.paymentservice.enums.PaymentMethod;
import iuh.paymentservice.enums.PaymentStatus;
import iuh.paymentservice.repositories.PaymentRepository;
import iuh.paymentservice.services.PaymentService;
import iuh.paymentservice.utils.VNPayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private VNPayConfig vnPayConfig;

    @Override
    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        // Tạo một entity Payment mới
        Payment payment = new Payment();
        payment.setOrderId(paymentRequest.getOrderId());
        payment.setUserId(paymentRequest.getUserId());
        payment.setAmount(paymentRequest.getAmount());
        payment.setCurrency(paymentRequest.getCurrency());
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);

        // Lưu vào database
        payment = paymentRepository.save(payment);

        // Nếu phương thức thanh toán là VNPay
        if (payment.getPaymentMethod() == PaymentMethod.VN_PAY) {
            // Tạo URL thanh toán VNPay
            String paymentUrl = createVNPayPaymentUrl(paymentRequest);

            // Trả về response với URL thanh toán
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

        // Nếu không phải VNPay, chỉ trả về thông tin Payment bình thường
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

    private String createVNPayPaymentUrl(PaymentRequest paymentRequest) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = paymentRequest.getOrderId();
        String vnp_IpAddr = paymentRequest.getIpAddress();
        String vnp_TmnCode = vnPayConfig.getTmnCode();
        String orderInfo = paymentRequest.getOrderInfo();
        long amount = (long) (paymentRequest.getAmount() * 100); // Chuyển đổi sang số tiền VNPay (VND * 100)

        Map<String, String> vnp_Params = new HashMap<>();
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
        if (locate != null && !locate.isEmpty()) {
            vnp_Params.put("vnp_Locale", locate);
        } else {
            vnp_Params.put("vnp_Locale", "vn");
        }

        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                hashData.append(fieldName).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8)).append('&');
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8)).append('=')
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8)).append('&');
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayUtils.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        queryUrl += "vnp_SecureHash=" + vnp_SecureHash;
        return vnPayConfig.getPaymentUrl() + "?" + queryUrl;
    }

    @Override
    public VNPayResponse processVNPayCallback(Map<String, String> vnpParams) {
        String vnp_ResponseCode = vnpParams.get("vnp_ResponseCode");
        String vnp_TxnRef = vnpParams.get("vnp_TxnRef");
        String vnp_Amount = vnpParams.get("vnp_Amount");
        String vnp_TransactionNo = vnpParams.get("vnp_TransactionNo");
        String vnp_BankCode = vnpParams.get("vnp_BankCode");
        String vnp_PayDate = vnpParams.get("vnp_PayDate");
        String vnp_OrderInfo = vnpParams.get("vnp_OrderInfo");

        // Xác thực chữ ký từ VNPay
        String vnp_SecureHash = vnpParams.get("vnp_SecureHash");
        Map<String, String> validationParams = new HashMap<>();

        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            if (!entry.getKey().equals("vnp_SecureHash") && !entry.getKey().equals("vnp_SecureHashType")) {
                validationParams.put(entry.getKey(), entry.getValue());
            }
        }

        String calculatedHash = VNPayUtils.hashAllFields(validationParams, vnPayConfig.getHashSecret());

        // Kiểm tra chữ ký và cập nhật trạng thái thanh toán
        if (calculatedHash.equals(vnp_SecureHash)) {
            // Tìm payment dựa trên orderId (vnp_TxnRef)
            Optional<Payment> paymentOptional = paymentRepository.findByOrderId(vnp_TxnRef);

            if (paymentOptional.isPresent()) {
                Payment payment = paymentOptional.get();

                // Nếu thanh toán thành công
                if ("00".equals(vnp_ResponseCode)) {
                    payment.setPaymentStatus(PaymentStatus.COMPLETED);
                    payment.setTransactionId(vnp_TransactionNo);
                } else {
                    // Thanh toán thất bại
                    payment.setPaymentStatus(PaymentStatus.FAILED);
                }

                paymentRepository.save(payment);
            }
        }

        // Trả về thông tin từ VNPay
        return VNPayResponse.builder()
                .vnpResponseCode(vnp_ResponseCode)
                .vnpTxnRef(vnp_TxnRef)
                .vnpAmount(vnp_Amount)
                .vnpTransactionNo(vnp_TransactionNo)
                .vnpBankCode(vnp_BankCode)
                .vnpPayDate(vnp_PayDate)
                .vnpOrderInfo(vnp_OrderInfo)
                .build();
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
}