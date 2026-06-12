package com.example.base_spring_boot.models.dtos.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CourtImageRes {
    private Long id;
    private String imageUrl;
    private Integer displayOrder;
    private LocalDateTime uploadedAt;
}

