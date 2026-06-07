package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.Repositories.PasswordResetTokenRepository;
import com.sany3.graduation_project.Repositories.UserRepository;
import com.sany3.graduation_project.entites.PasswordResetToken;
import com.sany3.graduation_project.entites.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRY_MINUTES = 15;
    private static final int MAX_REQUESTS_PER_HOUR = 5;
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    /**
     * Step 1: Request password reset — generates code and sends email
     * Always returns success (even if email doesn't exist) to prevent email enumeration
     */
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            log.info("Password reset requested for non-existent email: {}", email);
            // Don't reveal that email doesn't exist
            return;
        }

        // Rate limit: max 5 requests per hour
        long recentRequests = resetTokenRepository.countRecentTokensByUserId(
                user.getId(), LocalDateTime.now().minusHours(1));

        if (recentRequests >= MAX_REQUESTS_PER_HOUR) {
            log.warn("Rate limit exceeded for password reset: user {}", user.getId());
            throw new IllegalStateException("Too many reset requests. Please try again later.");
        }

        // Generate 6-digit code
        String code = generateCode();

        // Save token
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .code(code)
                .used(false)
                .attempts(0)
                .expiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES))
                .build();

        resetTokenRepository.save(token);

        // Send email
        emailService.sendPasswordResetCode(email, code);

        log.info("Password reset code generated for user {}", user.getId());
    }

    /**
     * Step 2: Verify the 6-digit code — returns a one-time reset token (UUID)
     */
    @Transactional
    public String verifyResetCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or code"));

        PasswordResetToken token = resetTokenRepository.findValidToken(
                        user.getId(), code, LocalDateTime.now())
                .orElse(null);

        if (token == null) {
            log.warn("Invalid or expired reset code for user {}", user.getId());
            throw new IllegalArgumentException("Invalid or expired code");
        }

        // Check max attempts
        if (token.getAttempts() >= MAX_VERIFY_ATTEMPTS) {
            log.warn("Max verification attempts reached for user {}", user.getId());
            token.setUsed(true); // Invalidate the token
            resetTokenRepository.save(token);
            throw new IllegalStateException("Too many attempts. Please request a new code.");
        }

        token.incrementAttempts();

        // Generate one-time reset token (UUID)
        String resetToken = UUID.randomUUID().toString();
        token.setResetToken(resetToken);
        resetTokenRepository.save(token);

        log.info("Reset code verified for user {}, reset token generated", user.getId());
        return resetToken;
    }

    /**
     * Step 3: Reset password using the one-time reset token
     */
    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        PasswordResetToken token = resetTokenRepository.findByResetTokenAndUsedFalse(resetToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        if (token.isExpired()) {
            throw new IllegalArgumentException("Reset token has expired. Please request a new code.");
        }

        // Update password
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        token.setUsed(true);
        resetTokenRepository.save(token);

        log.info("Password reset successful for user {}", user.getId());
    }

    /**
     * Generate a random 6-digit code
     */
    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // 100000-999999
        return String.valueOf(code);
    }
}
