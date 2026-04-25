package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity
 * Handles database operations for users (customers & providers)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     * @param email User's email
     * @return User if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by phone
     * @param phone User's phone number
     * @return User if found
     */
    Optional<User> findByPhone(String phone);

    /**
     * Check if email already exists
     * @param email Email to check
     * @return true if exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if phone already exists
     * @param phone Phone to check
     * @return true if exists
     */
    boolean existsByPhone(String phone);

    /**
     * Find all service providers (users with SERVICE_PROVIDER role)
     * @param pageable Pagination info
     * @return Page of providers
     */
    @Query("SELECT u FROM User u JOIN u.userRoles ur JOIN ur.role r " +
            "WHERE r.name = 'SERVICE_PROVIDER'")
    Page<User> findAllServiceProviders(Pageable pageable);

    /**
     * Find all customers
     * @param pageable Pagination info
     * @return Page of customers
     */
    @Query("SELECT u FROM User u JOIN u.userRoles ur JOIN ur.role r " +
            "WHERE r.name = 'USER'")
    Page<User> findAllCustomers(Pageable pageable);

    /**
     * Find providers near a location (within radius in km)
     * Uses Haversine formula for distance calculation
     *
     * @param latitude Customer's latitude
     * @param longitude Customer's longitude
     * @param radiusKm Search radius in kilometers
     * @return List of nearby providers
     */
    @Query(value = "SELECT u.* FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "JOIN roles r ON ur.role_id = r.id " +
            "WHERE r.name = 'SERVICE_PROVIDER' " +
            "AND u.is_active = true " +
            "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(u.latitude)) * " +
            "cos(radians(u.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(u.latitude)))) <= :radiusKm",
            nativeQuery = true)
    List<User> findProvidersNearby(@Param("latitude") BigDecimal latitude,
                                   @Param("longitude") BigDecimal longitude,
                                   @Param("radiusKm") Double radiusKm);

    /**
     * Find providers by service type near a location
     *
     * @param serviceType Type of service (GAS, WATER, ELECTRICITY)
     * @param latitude Customer's latitude
     * @param longitude Customer's longitude
     * @param radiusKm Search radius in kilometers
     * @return List of matching providers
     */
    @Query(value = "SELECT u.* FROM users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "JOIN roles r ON ur.role_id = r.id " +
            "JOIN service_provider_profiles spp ON u.id = spp.user_id " +
            "WHERE r.name = 'SERVICE_PROVIDER' " +
            "AND u.is_active = true " +
            "AND spp.service_type = :serviceType " +
            "AND spp.is_verified = true " +
            "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(u.latitude)) * " +
            "cos(radians(u.longitude) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(u.latitude)))) <= :radiusKm",
            nativeQuery = true)
    List<User> findProvidersByServiceTypeNearby(
            @Param("serviceType") String serviceType,
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusKm") Double radiusKm);
}