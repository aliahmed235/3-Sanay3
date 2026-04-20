package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.Role;
import com.sany3.graduation_project.entites.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
}