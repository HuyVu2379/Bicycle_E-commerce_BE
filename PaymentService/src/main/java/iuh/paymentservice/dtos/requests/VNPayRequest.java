package iuh.paymentservice.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VNPayRequest {
    private String orderType;
    private long amount;
    private String orderInfo;
    private String bankCode;
    private String language;
    private String orderId;
    private String ipAddress;
}