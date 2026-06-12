package com.example.base_spring_boot.models.services;

import com.example.base_spring_boot.models.dtos.res.CourtRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICourtService {
    Page<CourtRes> findAll(Pageable pageable);
    CourtRes findById(Long id);
}
