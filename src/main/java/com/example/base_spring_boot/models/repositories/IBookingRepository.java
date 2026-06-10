package com.example.base_spring_boot.models.repositories;

import com.example.base_spring_boot.models.entities.Booking;
import com.example.base_spring_boot.models.entities.Court;
import com.example.base_spring_boot.models.entities.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IBookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookingDate(LocalDate date);
    Optional<Booking> findByCourtAndSlotAndBookingDate(Court court, Slot slot, LocalDate date);
    org.springframework.data.domain.Page<Booking> findByUserOrderByBookingDateDesc(com.example.base_spring_boot.models.entities.User user, org.springframework.data.domain.Pageable pageable);
}
