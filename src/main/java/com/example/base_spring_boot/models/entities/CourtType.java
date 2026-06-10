package com.example.base_spring_boot.models.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "court_types")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CourtType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Ví dụ: Sân thảm, Sân gỗ, Sân ngoài trời

    private String description;
}
