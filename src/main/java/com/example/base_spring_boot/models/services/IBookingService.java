package com.example.base_spring_boot.models.services;

import com.example.base_spring_boot.models.dtos.req.BookingApprovalRequest;
import com.example.base_spring_boot.models.dtos.req.BookingReq;
import com.example.base_spring_boot.models.dtos.res.BookingApprovalResponse;
import com.example.base_spring_boot.models.dtos.res.BookingHistoryResponse;
import com.example.base_spring_boot.models.dtos.res.BookingRes;
import com.example.base_spring_boot.models.dtos.res.SlotRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface IBookingService {
    BookingRes createBooking(BookingReq req);
    Page<BookingRes> getMyBookings(Pageable pageable);
    List<SlotRes> getAvailableSlots(Long courtId, LocalDate date);
    List<BookingHistoryResponse> getBookingHistory();
    BookingApprovalResponse approveOrRejectBooking(Long bookingId, BookingApprovalRequest req);
}
