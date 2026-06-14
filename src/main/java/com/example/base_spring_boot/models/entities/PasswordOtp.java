package com.example.base_spring_boot.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_otps")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordOtp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
