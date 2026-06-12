package com.example.base_spring_boot.controllers;

import com.example.base_spring_boot.models.dtos.req.BookingApprovalRequest;
import com.example.base_spring_boot.models.dtos.wrapper.DataRes;
import com.example.base_spring_boot.models.services.IBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingAdminController {
    private final IBookingService bookingService;

    /**
     * Approve or reject a booking
     * Only ADMIN or MANAGER roles are allowed
     *
     * @param bookingId The ID of the booking to approve/reject
     * @param req The approval request with status (CONFIRMED or REJECTED)
     * @return BookingApprovalResponse
     */
    @PatchMapping("/{bookingId}/approval")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<?> approveOrRejectBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody BookingApprovalRequest req) {
        return ResponseEntity.ok(
                DataRes.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .message("Booking approved successfully")
                        .data(bookingService.approveOrRejectBooking(bookingId, req))
                        .build()
        );
    }
}

