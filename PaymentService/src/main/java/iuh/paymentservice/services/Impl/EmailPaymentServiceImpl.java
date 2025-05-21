package iuh.paymentservice.services.Impl;

import iuh.paymentservice.services.EmailService;
import iuh.paymentservice.services.PaymentService;

public class EmailPaymentServiceImpl implements EmailService {
    @Override
    public void sendEmail(String to, String subject, String body) {

    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath) {

    }

    @Override
    public void sendEmailWithInlineImage(String to, String subject, String body, String imagePath) {

    }
}
