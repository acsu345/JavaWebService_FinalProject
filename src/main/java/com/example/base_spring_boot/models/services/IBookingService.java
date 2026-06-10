package com.example.base_spring_boot.models.services;

import com.example.base_spring_boot.models.dtos.req.BookingReq;
import com.example.base_spring_boot.models.dtos.res.BookingRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IBookingService {
    BookingRes createBooking(BookingReq req);
    Page<BookingRes> getMyBookings(Pageable pageable);
}
