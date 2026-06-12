package com.example.base_spring_boot.models.dtos.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtImageUrlsReq {
    private List<CourtImageUrlReq> images;
}
