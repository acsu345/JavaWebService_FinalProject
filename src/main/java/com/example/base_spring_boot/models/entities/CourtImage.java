package com.example.base_spring_boot.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "court_images")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CourtImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "court_id", nullable = false)
    private Court court;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String imageUrl; // URL from Cloudinary

    @Column(name = "public_id")
    private String publicId; // Cloudinary public ID for deletion

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "display_order")
    private Integer displayOrder; // For ordering images

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}

