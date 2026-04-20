package com.sany3.graduation_project.Services;


import com.sany3.graduation_project.Repositories.*;
import com.sany3.graduation_project.dto.request.RegisterProviderRequest;
import com.sany3.graduation_project.dto.request.RegisterRequest;
import com.sany3.graduation_project.dto.response.LoginResponse;
import com.sany3.graduation_project.entites.*;
import com.sany3.graduation_project.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sany3.graduation_project.exception.InvalidCredentialsException;
import com.sany3.graduation_project.exception.UserAlreadyExistsException;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final ServiceProviderProfileRepository providerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public LoginResponse registerCustomer(RegisterRequest request) {
        log.info("Registering customer: {}", request.getEmail());

        validateUserDoesNotExist(request.getEmail(), request.getPhone());

        User user = createAndSaveUser(
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getPassword(),
                null, null, null
        );

        assignRoleToUser(user, RoleType.USER);
        createCustomerProfile(user);

        return generateLoginResponse(user);
    }

    public LoginResponse registerServiceProvider(RegisterProviderRequest request) {
        log.info("Registering service provider: {}", request.getEmail());

        validateUserDoesNotExist(request.getEmail(), request.getPhone());
        validateProviderData(request);

        User user = createAndSaveUser(
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getPassword(),
                request.getAddress(),
                request.getLatitude(),
                request.getLongitude()
        );

        assignRoleToUser(user, RoleType.SERVICE_PROVIDER);
        createServiceProviderProfile(user, request);

        return generateLoginResponse(user);
    }


    public LoginResponse login(String email, String password) {
        log.info("Login attempt for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Account is disabled");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        log.info("Login successful for: {}", email);
        return generateLoginResponse(user);
    }


    public LoginResponse refreshToken(String refreshToken) {
        log.info("Token refresh attempt");

        if (!jwtService.validateToken(refreshToken)) {
            throw new InvalidCredentialsException("Invalid or expired refresh token");
        }

        Long userId = jwtService.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtService.generateAccessToken(user);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .expiresIn(15 * 60L)
                .user(userMapper.toUserResponse(user))
                .build();
    }


    public void logout(String token) {
        log.info("Logout initiated");
    }



    private void validateUserDoesNotExist(String email, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already registered");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new UserAlreadyExistsException("Phone already registered");
        }
    }

    private void validateProviderData(RegisterProviderRequest request) {
        if (request.getHourlyRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Hourly rate must be greater than 0");
        }
        if (request.getBio() == null || request.getBio().trim().isEmpty()) {
            throw new IllegalArgumentException("Bio is required");
        }
    }

    private User createAndSaveUser(String name, String email, String phone, String password,
                                   String address, java.math.BigDecimal latitude,
                                   java.math.BigDecimal longitude) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setAddress(address);
        user.setLatitude(latitude);
        user.setLongitude(longitude);
        user.setIsActive(true);

        user = userRepository.save(user);
        log.debug("User created with ID: {}", user.getId());
        return user;
    }

    private void assignRoleToUser(User user, RoleType roleType) {
        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new RuntimeException(roleType + " role not found"));

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);

        log.debug("{} role assigned to user: {}", roleType, user.getId());
    }

    private void createCustomerProfile(User user) {
        CustomerProfile profile = new CustomerProfile();
        profile.setUser(user);
        customerProfileRepository.save(profile);
        log.debug("Customer profile created for user: {}", user.getId());
    }

    private void createServiceProviderProfile(User user, RegisterProviderRequest request) {
        ServiceProviderProfile profile = new ServiceProviderProfile();
        profile.setUser(user);
        profile.setServiceType(request.getServiceType());
        profile.setHourlyRate(request.getHourlyRate());
        profile.setBio(request.getBio());
        profile.setAddress(request.getAddress());
        profile.setLatitude(request.getLatitude());
        profile.setLongitude(request.getLongitude());
        profile.setIsVerified(false);
        profile.setVerificationStatus(VerificationStatus.PENDING);

        providerProfileRepository.save(profile);
        log.debug("Service provider profile created (PENDING) for user: {}", user.getId());
    }

    private LoginResponse generateLoginResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(15 * 60L)
                .user(userMapper.toUserResponse(user))
                .build();
    }
}