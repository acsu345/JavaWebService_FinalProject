package com.example.base_spring_boot.controllers;

import com.example.base_spring_boot.models.dtos.req.ChangePasswordReq;
import com.example.base_spring_boot.models.dtos.wrapper.DataRes;
import com.example.base_spring_boot.models.services.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordReq req) {
        userService.changePassword(req);
        return ResponseEntity.ok(DataRes.builder()
                .status(org.springframework.http.HttpStatus.OK)
                .code(200)
                .success(true)
                .message("Password changed successfully")
                .data(null)
                .build());
    }
}
