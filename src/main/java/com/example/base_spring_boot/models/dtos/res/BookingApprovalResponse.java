package com.example.base_spring_boot.models.dtos.res;

import com.example.base_spring_boot.models.constants.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BookingApprovalResponse {
    private Long bookingId;
    private BookingStatus status;
    private LocalDateTime updatedAt;
}
