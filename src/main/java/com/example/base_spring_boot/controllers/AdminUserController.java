package com.example.base_spring_boot.controllers;

import com.example.base_spring_boot.models.dtos.req.UserUpdateReq;
import com.example.base_spring_boot.models.dtos.wrapper.DataRes;
import com.example.base_spring_boot.models.services.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final IUserService userService;

    @GetMapping
    public ResponseEntity<?> getAllUsers(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(DataRes.builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(userService.findAll(pageable))
                .build());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String keyword, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(DataRes.builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(userService.search(keyword, pageable))
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(DataRes.builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(userService.findById(id))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateReq req) {
        return ResponseEntity.ok(DataRes.builder()
                .status(HttpStatus.OK)
                .code(200)
                .data(userService.update(id, req))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(DataRes.builder()
                .status(HttpStatus.OK)
                .code(200)
                .data("User deleted successfully")
                .build());
    }
}
