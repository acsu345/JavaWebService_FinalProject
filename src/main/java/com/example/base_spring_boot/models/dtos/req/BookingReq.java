package com.example.base_spring_boot.models.dtos.req;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BookingReq {
    @NotNull(message = "courtId must not be null")
    private Long courtId;

    @NotNull(message = "slotId must not be null")
    private Long slotId;

    @NotNull(message = "bookingDate must not be null")
    @FutureOrPresent(message = "bookingDate must be in the present or future")
    private LocalDate bookingDate;
}
