package com.example.base_spring_boot.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "performance_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String methodName;

    @Column(nullable = false)
    private long executionTime;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
