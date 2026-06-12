package com.example.base_spring_boot.models.repositories;

import com.example.base_spring_boot.models.entities.CourtImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICourtImageRepository extends JpaRepository<CourtImage, Long> {
    List<CourtImage> findByCourtIdOrderByDisplayOrder(Long courtId);

    void deleteByCourtId(Long courtId);

    int countByCourtId(Long courtId);
}

