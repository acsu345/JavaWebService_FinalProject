package com.example.base_spring_boot.models.services.impl;

import com.example.base_spring_boot.exceptions.HttpBadRequestException;
import com.example.base_spring_boot.exceptions.HttpNotFoundException;
import com.example.base_spring_boot.models.dtos.req.BookingReq;
import com.example.base_spring_boot.models.dtos.res.BookingRes;
import com.example.base_spring_boot.models.entities.Booking;
import com.example.base_spring_boot.models.entities.Court;
import com.example.base_spring_boot.models.entities.Slot;
import com.example.base_spring_boot.models.entities.User;
import com.example.base_spring_boot.models.repositories.IBookingRepository;
import com.example.base_spring_boot.models.repositories.ICourtRepository;
import com.example.base_spring_boot.models.repositories.ISlotRepository;
import com.example.base_spring_boot.models.repositories.IUserRepository;
import com.example.base_spring_boot.models.services.IBookingService;
import com.example.base_spring_boot.security.principal.MyUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements IBookingService {
    private final IBookingRepository bookingRepository;
    private final ICourtRepository courtRepository;
    private final ISlotRepository slotRepository;
    private final IUserRepository userRepository;

    @Override
    @Transactional
    public BookingRes createBooking(BookingReq req) {
        User user = getCurrentUser();
        Court court = courtRepository.findById(req.getCourtId())
                .orElseThrow(() -> new HttpNotFoundException("Court not found"));
        Slot slot = slotRepository.findById(req.getSlotId())
                .orElseThrow(() -> new HttpNotFoundException("Slot not found"));

        // Kiểm tra xem đã có ai đặt chưa
        boolean isAvailable = bookingRepository.findByCourtAndSlotAndBookingDate(court, slot, req.getBookingDate()).isEmpty();
        if (!isAvailable) {
            throw new HttpBadRequestException("This court and slot is already booked for the selected date");
        }

        Booking booking = Booking.builder()
                .user(user)
                .court(court)
                .slot(slot)
                .bookingDate(req.getBookingDate())
                .totalPrice(court.getPricePerHour()) // Giả sử 1 slot là 1 giờ, hoặc logic tính tiền khác
                .status("CONFIRMED")
                .build();

        return mapToBookingRes(bookingRepository.save(booking));
    }

    @Override
    public Page<BookingRes> getMyBookings(Pageable pageable) {
        User user = getCurrentUser();
        return bookingRepository.findByUserOrderByBookingDateDesc(user, pageable).map(this::mapToBookingRes);
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof MyUserDetails) {
            return ((MyUserDetails) principal).getUser();
        }
        // Fallback or throw exception if not authenticated correctly
        String username = principal.toString();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpNotFoundException("User not found"));
    }

    private BookingRes mapToBookingRes(Booking booking) {
        return BookingRes.builder()
                .id(booking.getId())
                .customerName(booking.getUser().getFullName())
                .courtName(booking.getCourt().getCourtName())
                .startTime(booking.getSlot().getStartTime())
                .endTime(booking.getSlot().getEndTime())
                .bookingDate(booking.getBookingDate())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .build();
    }
}
