package iuh.paymentservice.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Configuration
public class VNPayConfig {
    @Value("${payment.vnPay.url}")
    private String vnPay_Url;
    @Value("${payment.vnPay.returnUrl}")
    private String vnPay_returnUrl;
    @Value("${payment.vnPay.tmnCode}")
    private String tnmCode;
    @Value("${payment.vnPay.secretKey}")
    private String secret_key;
    @Value("${payment.vnPay.version}")
    private String vnp_version;
    @Value("${payment.vnPay.command}")
    private String vnp_command;
    @Value("${payment.vnPay.orderType}")
    private String orderType;

    public Map<String,String> getVNPayConfig(){
        Map<String,String> vnpParamsMap = new HashMap<>();
        vnpParamsMap.put("vnp_Version",this.vnp_version);
        vnpParamsMap.put("vnp_Command",this.vnp_version);
        vnpParamsMap.put("vnp_TmnCode",this.tnmCode);
//        vnpParamsMap.put("vnp_TxnRef",);
//        vnpParamsMap.put("vnp_OrderInfo","Thanh toan don hang: " + );
        vnpParamsMap.put("vnp_OrderType",this.orderType);
        vnpParamsMap.put("vnp_Locale","vn");
        vnpParamsMap.put("vnp_ReturnUrl",this.vnPay_returnUrl);
        Calendar calender = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = simpleDateFormat.format(calender.getTime());
        vnpParamsMap.put("vnp_CreateDate",vnpCreateDate);
        calender.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = simpleDateFormat.format(calender.getTime());
        vnpParamsMap.put("vnp_ExpireDate",vnp_ExpireDate);
        return vnpParamsMap;
    }

}
