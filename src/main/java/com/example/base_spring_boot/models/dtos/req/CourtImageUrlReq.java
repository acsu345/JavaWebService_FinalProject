package com.example.base_spring_boot.models.dtos.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtImageUrlReq {
    private String imageUrl;
    private Integer displayOrder;
}
