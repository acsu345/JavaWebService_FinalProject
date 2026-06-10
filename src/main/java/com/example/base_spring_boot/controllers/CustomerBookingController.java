package com.example.base_spring_boot.controllers;

import com.example.base_spring_boot.models.dtos.req.BookingReq;
import com.example.base_spring_boot.models.dtos.wrapper.DataRes;
import com.example.base_spring_boot.models.services.IBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer/bookings")
@RequiredArgsConstructor
public class CustomerBookingController {
    private final IBookingService bookingService;

    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                DataRes.builder()
                        .status(HttpStatus.CREATED)
                        .code(201)
                        .data(bookingService.createBooking(req))
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<?> getMyBookings(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(
                DataRes.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .data(bookingService.getMyBookings(pageable))
                        .build()
        );
    }
}
