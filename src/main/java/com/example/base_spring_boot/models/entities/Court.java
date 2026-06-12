package com.example.base_spring_boot.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courts")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Court {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String courtName; // Ví dụ: Sân số 1, Sân số 2

    @ManyToOne
    @JoinColumn(name = "court_type_id", nullable = false)
    private CourtType courtType;

    private Double pricePerHour;

    private String status; // Ví dụ: AVAILABLE, MAINTENANCE

    @OneToMany(mappedBy = "court", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CourtImage> images = new ArrayList<>();
}
