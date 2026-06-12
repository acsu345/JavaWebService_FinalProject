package com.example.base_spring_boot.models.dtos.res;

import com.example.base_spring_boot.models.constants.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BookingHistoryResponse {
    private Long bookingId;
    private Long courtId;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BookingStatus status;
}
