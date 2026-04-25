package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for UserRole entity (Many-to-Many mapping)
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

}