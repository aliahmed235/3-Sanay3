package com.sany3.graduation_project.Services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email service for sending password reset codes.
 * Gracefully degrades when mail is not configured.
 */
@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@sany3.com}")
    private String fromEmail;

    /**
     * Send password reset code to user's email
     */
    public void sendPasswordResetCode(String toEmail, String code) {
        if (mailSender == null) {
            log.warn("Mail not configured — reset code for {}: {}", toEmail, code);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Sany3 - Password Reset Code");
            message.setText(
                    "Your password reset code is: " + code + "\n\n" +
                    "This code expires in 15 minutes.\n\n" +
                    "If you did not request this, please ignore this email.\n\n" +
                    "- Sany3 Team"
            );

            mailSender.send(message);
            log.info("Password reset code sent to {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            // Don't throw — user shouldn't know if email failed (security)
        }
    }

    /**
     * Check if email sending is available
     */
    public boolean isMailConfigured() {
        return mailSender != null;
    }
}
