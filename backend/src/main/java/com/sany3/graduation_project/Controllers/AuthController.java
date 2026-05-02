package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Services.AuthService;
import com.sany3.graduation_project.Services.AuthServiceImpl;
import com.sany3.graduation_project.dto.request.LoginRequest;
import com.sany3.graduation_project.dto.request.RefreshTokenRequest;
import com.sany3.graduation_project.dto.request.RegisterProviderRequest;
import com.sany3.graduation_project.dto.request.RegisterRequest;
import com.sany3.graduation_project.dto.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth")
@Validated
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> registerCustomer(@Valid @RequestBody RegisterRequest request) {
        log.info("Customer registration request: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registerCustomer(request));
    }

    @PostMapping(value = "/register/provider", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerServiceProvider(@Valid @RequestBody RegisterProviderRequest request) {
        log.info("Service provider registration request: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registerServiceProvider(request));
    }

    @PostMapping(value = "/register/provider", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerServiceProviderWithFiles(
            @Valid @ModelAttribute RegisterProviderRequest request,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestPart(value = "criminalHistory", required = false) MultipartFile criminalHistory) {
        log.info("Service provider multipart registration request: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.registerServiceProvider(request, profilePicture, criminalHistory));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt: {}", request.getEmail());
        return ResponseEntity.ok(authService.login(request.getEmail(), request.getPassword()));
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh attempt");
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        log.info("Logout attempt");
        authService.logout(token);
        return ResponseEntity.ok(new SuccessResponse("Logout successful", true));
    }
}
