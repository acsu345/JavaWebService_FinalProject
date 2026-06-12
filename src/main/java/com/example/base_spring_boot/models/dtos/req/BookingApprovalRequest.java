package com.example.base_spring_boot.models.dtos.req;

import com.example.base_spring_boot.models.constants.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BookingApprovalRequest {
    @NotNull(message = "status must not be null")
    private BookingStatus status;
}
