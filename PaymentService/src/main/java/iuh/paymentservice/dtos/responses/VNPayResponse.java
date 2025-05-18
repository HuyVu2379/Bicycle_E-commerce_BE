package iuh.paymentservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VNPayResponse {
    private String paymentUrl;
    private String vnpTxnRef;
    private String vnpOrderInfo;
    private String vnpResponseCode;
    private String vnpTransactionNo;
    private String vnpBankCode;
    private String vnpAmount;
    private String vnpPayDate;
}