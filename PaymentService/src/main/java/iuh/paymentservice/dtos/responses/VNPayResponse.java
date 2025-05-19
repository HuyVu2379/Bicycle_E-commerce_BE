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
    private String vnpResponseCode;
    private String vnpTxnRef;
    private String vnpAmount;
    private String vnpTransactionNo;
    private String vnpBankCode;
    private String vnpPayDate;
    private String vnpOrderInfo;
    private String paymentId;
}