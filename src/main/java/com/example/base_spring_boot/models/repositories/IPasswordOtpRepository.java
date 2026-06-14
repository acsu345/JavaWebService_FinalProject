package com.example.base_spring_boot.models.repositories;

import com.example.base_spring_boot.models.entities.PasswordOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPasswordOtpRepository extends JpaRepository<PasswordOtp, Long> {
    Optional<PasswordOtp> findByEmailAndOtp(String email, String otp);
    void deleteByEmail(String email);
}
