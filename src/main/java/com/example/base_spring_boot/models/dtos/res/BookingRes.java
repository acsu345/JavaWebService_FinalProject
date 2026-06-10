package com.example.base_spring_boot.models.dtos.res;

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
public class BookingRes {
    private Long id;
    private String customerName;
    private String courtName;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDate bookingDate;
    private Double totalPrice;
    private String status;
}
