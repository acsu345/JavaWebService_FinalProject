package com.example.base_spring_boot.models.services.impl;

import com.example.base_spring_boot.exceptions.HttpBadRequestException;
import com.example.base_spring_boot.exceptions.HttpConflictException;
import com.example.base_spring_boot.exceptions.HttpNotFoundException;
import com.example.base_spring_boot.models.constants.BookingStatus;
import com.example.base_spring_boot.models.dtos.req.BookingApprovalRequest;
import com.example.base_spring_boot.models.dtos.req.BookingReq;
import com.example.base_spring_boot.models.dtos.res.BookingApprovalResponse;
import com.example.base_spring_boot.models.dtos.res.BookingHistoryResponse;
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

import com.example.base_spring_boot.models.dtos.res.SlotRes;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements IBookingService {
    private final IBookingRepository bookingRepository;
    private final ICourtRepository courtRepository;
    private final ISlotRepository slotRepository;
    private final IUserRepository userRepository;

    @Override
    public List<SlotRes> getAvailableSlots(Long courtId, LocalDate date) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new HttpNotFoundException("Court not found"));
        
        List<Slot> allSlots = slotRepository.findAll();
        List<Booking> bookedOnDate = bookingRepository.findByBookingDate(date);
        
        List<Long> bookedSlotIds = bookedOnDate.stream()
                .filter(b -> b.getCourt().getId().equals(courtId))
                .map(b -> b.getSlot().getId())
                .collect(Collectors.toList());

        return allSlots.stream().map(slot -> SlotRes.builder()
                .id(slot.getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .available(!bookedSlotIds.contains(slot.getId()))
                .build()
        ).collect(Collectors.toList());
    }

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
                .status(BookingStatus.PENDING)
                .build();

        return mapToBookingRes(bookingRepository.save(booking));
    }

    @Override
    public Page<BookingRes> getMyBookings(Pageable pageable) {
        User user = getCurrentUser();
        return bookingRepository.findByUserOrderByBookingDateDesc(user, pageable).map(this::mapToBookingRes);
    }

    @Override
    public List<BookingHistoryResponse> getBookingHistory() {
        User user = getCurrentUser();
        return bookingRepository.findAll().stream()
                .filter(booking -> booking.getUser().getId().equals(user.getId()))
                .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()))
                .map(booking -> BookingHistoryResponse.builder()
                        .bookingId(booking.getId())
                        .courtId(booking.getCourt().getId())
                        .bookingDate(booking.getBookingDate())
                        .startTime(booking.getSlot().getStartTime())
                        .endTime(booking.getSlot().getEndTime())
                        .status(booking.getStatus())
                        .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingApprovalResponse approveOrRejectBooking(Long bookingId, BookingApprovalRequest req) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new HttpNotFoundException("Booking not found"));

        // Check if booking is PENDING
        if (!booking.getStatus().equals(BookingStatus.PENDING)) {
            throw new HttpConflictException("Booking is not in PENDING status");
        }

        // Check if requested status is valid (only CONFIRMED or REJECTED)
        BookingStatus requestedStatus = req.getStatus();
        if (!requestedStatus.equals(BookingStatus.CONFIRMED) && !requestedStatus.equals(BookingStatus.REJECTED)) {
            throw new HttpBadRequestException("Invalid status. Only CONFIRMED or REJECTED are allowed");
        }

        // Update booking status
        booking.setStatus(requestedStatus);
        bookingRepository.save(booking);

        return BookingApprovalResponse.builder()
                .bookingId(booking.getId())
                .status(booking.getStatus())
                .updatedAt(booking.getUpdatedAt())
                .build();
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
                .status(booking.getStatus().toString())
                .build();

    }
}
