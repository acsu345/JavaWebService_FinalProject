package com.example.base_spring_boot.controllers;

import com.example.base_spring_boot.models.dtos.req.ForgotPasswordReq;
import com.example.base_spring_boot.models.dtos.req.LoginReq;
import com.example.base_spring_boot.models.dtos.req.RegisterReq;
import com.example.base_spring_boot.models.dtos.req.ResetPasswordReq;
import com.example.base_spring_boot.models.dtos.wrapper.DataRes;
import com.example.base_spring_boot.models.services.IAuthService;
import com.example.base_spring_boot.models.services.IJwtBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController
{
    private final IAuthService authService;
    private final IJwtBlacklistService jwtBlacklistService;

    /**
     * @param req LoginReq
     * @apiNote handle login with { username , password }
     */
    @PostMapping("/login")
    public ResponseEntity<?> handleLogin(@Valid @RequestBody LoginReq req)
    {
        return ResponseEntity.status(HttpStatus.OK).body(
                DataRes.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .success(true)
                        .message("Login successfully")
                        .data(authService.login(req))
                        .build()
        );
    }

    /**
     * @param req RegisterReq
     * @apiNote handle register with { fullName , username , password }
     */
    @PostMapping("/register")
    public ResponseEntity<?> handleRegister(@Valid @RequestBody RegisterReq req)
    {
        authService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                DataRes.builder()
                        .status(HttpStatus.CREATED)
                        .code(201)
                        .success(true)
                        .message("Register successfully")
                        .data(null)
                        .build()
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> handleRefreshToken(@RequestBody Map<String, String> req)
    {
        String refreshToken = req.get("refreshToken");
        return ResponseEntity.ok(
                DataRes.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .success(true)
                        .message("Token refreshed")
                        .data(authService.refreshToken(refreshToken))
                        .build()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> handleLogout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            jwtBlacklistService.blacklistToken(token);
        }
        return ResponseEntity.ok(
                DataRes.builder()
                        .status(HttpStatus.OK)
                        .code(200)
                        .success(true)
                        .message("Logout successfully")
                        .data(null)
                        .build()
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordReq req) {
        authService.forgotPassword(req);
        return ResponseEntity.ok(DataRes.builder()
                .status(HttpStatus.OK)
                .code(200)
                .success(true)
                .message("OTP has been sent to your email")
                .data(null)
                .build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordReq req) {
        authService.resetPassword(req);
        return ResponseEntity.ok(DataRes.builder()
                .status(HttpStatus.OK)
                .code(200)
                .success(true)
                .message("Password reset successfully")
                .data(null)
                .build());
    }
}