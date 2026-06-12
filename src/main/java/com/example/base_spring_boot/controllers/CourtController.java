package com.example.base_spring_boot.controllers;

import com.example.base_spring_boot.models.dtos.wrapper.DataRes;
import com.example.base_spring_boot.models.services.ICourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/courts")
@RequiredArgsConstructor
public class CourtController {
    private final ICourtService courtService;

    @GetMapping
    public ResponseEntity<?> getAllCourts(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(DataRes.builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(courtService.findAll(pageable))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCourtById(@PathVariable Long id) {
        return ResponseEntity.ok(DataRes.builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(courtService.findById(id))
                .build());
    }
}
