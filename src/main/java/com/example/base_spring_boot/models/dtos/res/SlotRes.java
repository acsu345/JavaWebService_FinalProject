package com.example.base_spring_boot.models.dtos.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SlotRes {
    private Long id;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
}
