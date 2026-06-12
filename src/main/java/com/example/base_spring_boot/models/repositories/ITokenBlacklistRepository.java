package com.example.base_spring_boot.models.repositories;

import com.example.base_spring_boot.models.entities.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    Optional<TokenBlacklist> findByToken(String token);
    boolean existsByToken(String token);
}
