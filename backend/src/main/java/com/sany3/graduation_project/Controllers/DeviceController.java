package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Repositories.UserFcmTokenRepository;
import com.sany3.graduation_project.Repositories.UserRepository;
import com.sany3.graduation_project.dto.request.RegisterFcmTokenRequest;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.entites.User;
import com.sany3.graduation_project.entites.UserFcmToken;
import com.sany3.graduation_project.exception.ResourceNotFoundException;
import com.sany3.graduation_project.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DeviceController {

    private final UserFcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;

    /**
     * POST /api/devices/fcm-token — Register or update an FCM token
     */
    @PostMapping("/fcm-token")
    public ResponseEntity<ApiResponse<String>> registerFcmToken(
            @Valid @RequestBody RegisterFcmTokenRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        log.info("Registering FCM token for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if token already exists
        Optional<UserFcmToken> existing = fcmTokenRepository.findByToken(request.getToken());

        if (existing.isPresent()) {
            // Token exists — update ownership and reactivate
            UserFcmToken token = existing.get();
            token.setUser(user);
            token.setPlatform(request.getPlatform());
            token.setActive(true);
            token.setLastSeenAt(LocalDateTime.now());
            fcmTokenRepository.save(token);
            log.info("Updated existing FCM token {} for user {}", token.getId(), userId);
        } else {
            // New token
            UserFcmToken token = UserFcmToken.builder()
                    .user(user)
                    .token(request.getToken())
                    .platform(request.getPlatform())
                    .active(true)
                    .lastSeenAt(LocalDateTime.now())
                    .build();
            fcmTokenRepository.save(token);
            log.info("Registered new FCM token for user {}", userId);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Token registered", Constants.SUCCESS_MESSAGE.FCM_TOKEN_REGISTERED));
    }
}
