package com.sany3.graduation_project.Services;

import com.sany3.graduation_project.Repositories.CustomerProfileRepository;
import com.sany3.graduation_project.Repositories.ServiceProviderProfileRepository;
import com.sany3.graduation_project.Repositories.UserRepository;
import com.sany3.graduation_project.dto.request.ChangePasswordRequest;
import com.sany3.graduation_project.dto.request.UpdateAddressRequest;
import com.sany3.graduation_project.dto.request.UpdateHourlyRateRequest;
import com.sany3.graduation_project.dto.request.UpdateNameRequest;
import com.sany3.graduation_project.dto.response.UserResponse;
import com.sany3.graduation_project.entites.CustomerProfile;
import com.sany3.graduation_project.entites.ServiceProviderProfile;
import com.sany3.graduation_project.entites.User;
import com.sany3.graduation_project.exception.ResourceNotFoundException;
import com.sany3.graduation_project.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final ServiceProviderProfileRepository providerProfileRepository;
    private final CloudinaryStorageService cloudinaryStorageService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * Get current user's profile
     */
    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        User user = findUserById(userId);
        return userMapper.toUserResponse(user);
    }

    /**
     * Update user's name
     */
    @Transactional
    public UserResponse updateName(Long userId, UpdateNameRequest request) {
        User user = findUserById(userId);
        user.setName(request.getName());
        userRepository.save(user);
        log.info("User {} updated name to '{}'", userId, request.getName());
        return userMapper.toUserResponse(user);
    }

    /**
     * Change user's password
     * Requires current password for security
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findUserById(userId);

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Prevent using the same password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("User {} changed password", userId);
    }

    /**
     * Upload or change profile photo
     */
    @Transactional
    public UserResponse updatePhoto(Long userId, MultipartFile photo) {
        User user = findUserById(userId);

        String imageUrl = cloudinaryStorageService.upload(photo, "profile-photos");
        user.setProfileImage(imageUrl);
        userRepository.save(user);
        log.info("User {} updated profile photo", userId);
        return userMapper.toUserResponse(user);
    }

    /**
     * Remove profile photo
     */
    @Transactional
    public UserResponse removePhoto(Long userId) {
        User user = findUserById(userId);
        user.setProfileImage(null);
        userRepository.save(user);
        log.info("User {} removed profile photo", userId);
        return userMapper.toUserResponse(user);
    }

    /**
     * Update address — works for both customer and provider
     * Updates on the User entity AND on the respective profile (customer or provider)
     */
    @Transactional
    public UserResponse updateAddress(Long userId, UpdateAddressRequest request) {
        User user = findUserById(userId);

        // Update on user entity
        user.setAddress(request.getAddress());
        user.setLatitude(request.getLatitude());
        user.setLongitude(request.getLongitude());
        userRepository.save(user);

        // Also update on the specific profile
        if (user.hasRole("SERVICE_PROVIDER")) {
            providerProfileRepository.findByUserId(userId).ifPresent(provider -> {
                provider.setAddress(request.getAddress());
                provider.setLatitude(request.getLatitude());
                provider.setLongitude(request.getLongitude());
                providerProfileRepository.save(provider);
            });
        } else {
            customerProfileRepository.findByUserId(userId).ifPresent(customer -> {
                customer.setAddress(request.getAddress());
                customer.setLatitude(request.getLatitude());
                customer.setLongitude(request.getLongitude());
                customerProfileRepository.save(customer);
            });
        }

        log.info("User {} updated address", userId);
        return userMapper.toUserResponse(user);
    }

    /**
     * Update hourly rate — provider only
     */
    @Transactional
    public UserResponse updateHourlyRate(Long userId, UpdateHourlyRateRequest request) {
        User user = findUserById(userId);

        // Must be a provider
        if (!user.hasRole("SERVICE_PROVIDER")) {
            throw new IllegalStateException("Only service providers can update hourly rate");
        }

        ServiceProviderProfile provider = providerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider profile not found"));

        provider.setHourlyRate(request.getHourlyRate());
        providerProfileRepository.save(provider);
        log.info("User {} updated hourly rate to {}", userId, request.getHourlyRate());
        return userMapper.toUserResponse(user);
    }

    private User findUserById(Long userId) {
        return userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
