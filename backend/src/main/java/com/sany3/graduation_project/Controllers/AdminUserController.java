package com.sany3.graduation_project.Controllers;

import com.sany3.graduation_project.Repositories.CustomerProfileRepository;
import com.sany3.graduation_project.Repositories.RoleRepository;
import com.sany3.graduation_project.Repositories.UserRepository;
import com.sany3.graduation_project.Repositories.UserRoleRepository;
import com.sany3.graduation_project.dto.request.RegisterRequest;
import com.sany3.graduation_project.dto.response.ApiResponse;
import com.sany3.graduation_project.dto.response.UserResponse;
import com.sany3.graduation_project.entites.*;
import com.sany3.graduation_project.exception.ResourceNotFoundException;
import com.sany3.graduation_project.exception.UserAlreadyExistsException;
import com.sany3.graduation_project.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * POST /admin/users/create-admin — Create a new admin account
     * Only existing admins can create other admins
     */
    @PostMapping("/create-admin")
    public ResponseEntity<ApiResponse<UserResponse>> createAdmin(
            @Valid @RequestBody RegisterRequest request,
            Authentication authentication) {

        Long adminId = (Long) authentication.getPrincipal();
        log.info("Admin {} creating new admin: {}", adminId, request.getEmail());

        // Validate email and phone don't exist
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException("Phone already registered");
        }

        // Create user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        user = userRepository.save(user);

        // Assign USER + ADMIN roles
        assignRole(user, RoleType.USER);
        assignRole(user, RoleType.ADMIN);

        // Create customer profile
        CustomerProfile profile = new CustomerProfile();
        profile.setUser(user);
        customerProfileRepository.save(profile);

        log.info("New admin created: {} (id={})", request.getEmail(), user.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(userMapper.toUserResponse(user), "Admin created successfully"));
    }

    /**
     * PUT /admin/users/{userId}/make-admin — Promote an existing user to admin
     */
    @PutMapping("/{userId}/make-admin")
    public ResponseEntity<ApiResponse<UserResponse>> makeAdmin(
            @PathVariable Long userId,
            Authentication authentication) {

        Long adminId = (Long) authentication.getPrincipal();
        log.info("Admin {} promoting user {} to ADMIN", adminId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.hasRole("ADMIN")) {
            return ResponseEntity.ok(ApiResponse.success(
                    userMapper.toUserResponse(user), "User is already an admin"));
        }

        assignRole(user, RoleType.ADMIN);

        log.info("User {} promoted to ADMIN by admin {}", userId, adminId);

        return ResponseEntity.ok(ApiResponse.success(
                userMapper.toUserResponse(user), "User promoted to admin successfully"));
    }

    /**
     * PUT /admin/users/{userId}/remove-admin — Remove admin role from a user
     */
    @PutMapping("/{userId}/remove-admin")
    public ResponseEntity<ApiResponse<UserResponse>> removeAdmin(
            @PathVariable Long userId,
            Authentication authentication) {

        Long adminId = (Long) authentication.getPrincipal();

        if (adminId.equals(userId)) {
            throw new IllegalStateException("Cannot remove your own admin role");
        }

        log.info("Admin {} removing ADMIN role from user {}", adminId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.hasRole("ADMIN")) {
            return ResponseEntity.ok(ApiResponse.success(
                    userMapper.toUserResponse(user), "User is not an admin"));
        }

        user.getUserRoles().stream()
                .filter(ur -> ur.getRole().getName() == RoleType.ADMIN)
                .findFirst()
                .ifPresent(ur -> {
                    userRoleRepository.delete(ur);
                    user.getUserRoles().remove(ur);
                });

        log.info("ADMIN role removed from user {} by admin {}", userId, adminId);

        return ResponseEntity.ok(ApiResponse.success(
                userMapper.toUserResponse(user), "Admin role removed successfully"));
    }

    private void assignRole(User user, RoleType roleType) {
        Role role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new RuntimeException(roleType + " role not found"));

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);
    }
}
