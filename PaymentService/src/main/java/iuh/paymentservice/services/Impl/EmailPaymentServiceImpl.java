package iuh.paymentservice.services.Impl;

import iuh.paymentservice.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
@Service
public class EmailPaymentServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender mailSender;
    private static final Logger logger = LoggerFactory.getLogger(EmailPaymentServiceImpl.class);
    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true để hỗ trợ HTML
            mailSender.send(mimeMessage);
            logger.info("Sent email to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email to: {}", to, e);
            // Không ném ngoại lệ để tránh ảnh hưởng giao dịch chính
        }
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath) {

    }

    @Override
    public void sendEmailWithInlineImage(String to, String subject, String body, String imagePath) {

    }
}
