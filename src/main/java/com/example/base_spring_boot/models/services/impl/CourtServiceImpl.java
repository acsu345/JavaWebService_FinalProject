package com.example.base_spring_boot.models.services.impl;

import com.example.base_spring_boot.exceptions.HttpNotFoundException;
import com.example.base_spring_boot.models.dtos.res.CourtRes;
import com.example.base_spring_boot.models.entities.Court;
import com.example.base_spring_boot.models.repositories.ICourtRepository;
import com.example.base_spring_boot.models.services.ICourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourtServiceImpl implements ICourtService {
    private final ICourtRepository courtRepository;

    @Override
    public Page<CourtRes> findAll(Pageable pageable) {
        return courtRepository.findAll(pageable).map(this::mapToCourtRes);
    }

    @Override
    public CourtRes findById(Long id) {
        Court court = courtRepository.findById(id)
                .orElseThrow(() -> new HttpNotFoundException("Court not found with id: " + id));
        return mapToCourtRes(court);
    }

    private CourtRes mapToCourtRes(Court court) {
        return CourtRes.builder()
                .id(court.getId())
                .courtName(court.getCourtName())
                .courtTypeName(court.getCourtType().getName())
                .pricePerHour(court.getPricePerHour())
                .status(court.getStatus())
                .build();
    }
}
