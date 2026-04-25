package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.Role;
import com.sany3.graduation_project.entites.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Role entity
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find role by name
     * @param name Role name (USER, SERVICE_PROVIDER, ADMIN)
     * @return Role if found
     */
    Optional<Role> findByName(RoleType name);
}