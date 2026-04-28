package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email (for login)
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by phone
     */
    Optional<User> findByPhone(String phone);

    /**
     * Check if email exists
     */
    Boolean existsByEmail(String email);

    /**
     * Check if phone exists
     */
    Boolean existsByPhone(String phone);

    /**
     * Find all active users
     */
    List<User> findByIsActiveTrue();

    /**
     * Find all providers (users with PROVIDER role)
     * Joins with user_roles table
     */
    @Query("SELECT u FROM User u JOIN UserRole ur ON u.id = ur.user.id WHERE ur.role.name = 'PROVIDER'")
    List<User> findAllProviders();

    /**
     * Find all customers (users with CUSTOMER role)
     * Joins with user_roles table
     */
    @Query("SELECT u FROM User u JOIN UserRole ur ON u.id = ur.user.id WHERE ur.role.name = 'CUSTOMER'")
    List<User> findAllCustomers();

    /**
     * Search users by name
     */
    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Get top rated providers
     */
    @Query("SELECT u FROM User u WHERE u.id IN " +
            "(SELECT r.provider.id FROM Rating r WHERE r.provider.id IS NOT NULL " +
            "GROUP BY r.provider.id ORDER BY AVG(r.finalRating) DESC LIMIT :limit)")
    List<User> findTopRatedProviders(@Param("limit") int limit);
}