package com.example.base_spring_boot.models.dtos.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CourtRes {
    private Long id;
    private String courtName;
    private String courtTypeName;
    private Double pricePerHour;
    private String status;
}
