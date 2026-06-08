package com.sany3.graduation_project.Services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Email service for sending password reset codes.
 * Uses Resend API (works on Railway where SMTP is blocked).
 * Falls back to logging the code when not configured.
 */
@Service
@Slf4j
public class EmailService {

    @Value("${resend.api-key:}")
    private String resendApiKey;

    @Value("${resend.from:Sany3 <onboarding@resend.dev>}")
    private String fromEmail;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Send password reset code to user's email
     */
    public void sendPasswordResetCode(String toEmail, String code) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn("Resend not configured — reset code for {}: {}", toEmail, code);
            return;
        }

        try {
            String jsonBody = """
                    {
                      "from": "%s",
                      "to": ["%s"],
                      "subject": "Sany3 - Password Reset Code",
                      "html": "<h2>Password Reset Code</h2><p>Your code is: <strong>%s</strong></p><p>This code expires in 15 minutes.</p><p>If you did not request this, please ignore this email.</p><br><p>— Sany3 Team</p>"
                    }
                    """.formatted(fromEmail, toEmail, code);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                log.info("Password reset code sent to {} via Resend", toEmail);
            } else {
                log.error("Resend API error ({}): {}", response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            // Don't throw — user shouldn't know if email failed (security)
        }
    }

    /**
     * Check if email sending is available
     */
    public boolean isMailConfigured() {
        return resendApiKey != null && !resendApiKey.isBlank();
    }
}
