package com.sany3.graduation_project.Repositories;

import com.sany3.graduation_project.entites.Wallet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<Wallet> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Override
    @EntityGraph(attributePaths = {"user"})
    List<Wallet> findAll();
}
