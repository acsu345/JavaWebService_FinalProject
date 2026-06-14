package com.example.base_spring_boot.models.services;

import com.example.base_spring_boot.exceptions.HttpBadRequestException;
import com.example.base_spring_boot.exceptions.HttpNotFoundException;
import com.example.base_spring_boot.models.dtos.req.ChangePasswordReq;
import com.example.base_spring_boot.models.dtos.req.ForgotPasswordReq;
import com.example.base_spring_boot.models.dtos.req.ResetPasswordReq;
import com.example.base_spring_boot.models.entities.PasswordOtp;
import com.example.base_spring_boot.models.entities.User;
import com.example.base_spring_boot.models.repositories.IPasswordOtpRepository;
import com.example.base_spring_boot.models.repositories.IUserRepository;
import com.example.base_spring_boot.models.services.impl.AuthServiceImpl;
import com.example.base_spring_boot.models.services.impl.UserServiceImpl;
import com.example.base_spring_boot.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceTests {

    @Mock
    private IUserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private IPasswordOtpRepository passwordOtpRepository;

    @Mock
    private IMailService mailService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private IRoleService roleService; // Cần thiết cho UserServiceImpl

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private AuthenticationManager authenticationManager; // Cần thiết cho AuthServiceImpl
    
    @Mock
    private JwtUtils jwtUtils; // Cần thiết cho AuthServiceImpl

    @BeforeEach
    void setUp() {
    }

    // 1. changePassword_Success
    @Test
    void changePassword_Success() {
        // Given
        ChangePasswordReq req = new ChangePasswordReq();
        req.setOldPassword("oldPass");
        req.setNewPassword("newPass");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedOldPass");

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "encodedOldPass")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        // When
        userService.changePassword(req);

        // Then
        verify(userRepository, times(1)).save(user);
        assertEquals("encodedNewPass", user.getPassword());
    }

    // 2. changePassword_InvalidOldPassword
    @Test
    void changePassword_InvalidOldPassword() {
        // Given
        ChangePasswordReq req = new ChangePasswordReq();
        req.setOldPassword("wrongPass");
        req.setNewPassword("newPass");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedOldPass");

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "encodedOldPass")).thenReturn(false);

        // When & Then
        assertThrows(HttpBadRequestException.class, () -> userService.changePassword(req));
        verify(userRepository, never()).save(any());
    }

    // 3. forgotPassword_GenerateOtpSuccess
    @Test
    void forgotPassword_GenerateOtpSuccess() {
        // Given
        ForgotPasswordReq req = new ForgotPasswordReq();
        req.setEmail("test@gmail.com");

        when(userRepository.existsByEmail("test@gmail.com")).thenReturn(true);

        // When
        authService.forgotPassword(req);

        // Then
        verify(passwordOtpRepository, times(1)).save(any(PasswordOtp.class));
        verify(mailService, times(1)).sendOtpEmail(eq("test@gmail.com"), anyString());
    }

    // 4. resetPassword_Success
    @Test
    void resetPassword_Success() {
        // Given
        ResetPasswordReq req = new ResetPasswordReq();
        req.setEmail("test@gmail.com");
        req.setOtp("123456");
        req.setNewPassword("newPass");

        PasswordOtp otpEntity = PasswordOtp.builder()
                .email("test@gmail.com")
                .otp("123456")
                .expiredAt(LocalDateTime.now().plusMinutes(10))
                .build();

        User user = new User();
        user.setEmail("test@gmail.com");

        when(passwordOtpRepository.findByEmailAndOtp("test@gmail.com", "123456")).thenReturn(Optional.of(otpEntity));
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        // When
        authService.resetPassword(req);

        // Then
        verify(userRepository, times(1)).save(user);
        verify(passwordOtpRepository, times(1)).delete(otpEntity);
        assertEquals("encodedNewPass", user.getPassword());
    }

    // 5. resetPassword_OtpExpired
    @Test
    void resetPassword_OtpExpired() {
        // Given
        ResetPasswordReq req = new ResetPasswordReq();
        req.setEmail("test@gmail.com");
        req.setOtp("123456");

        PasswordOtp otpEntity = PasswordOtp.builder()
                .email("test@gmail.com")
                .otp("123456")
                .expiredAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(passwordOtpRepository.findByEmailAndOtp("test@gmail.com", "123456")).thenReturn(Optional.of(otpEntity));

        // When & Then
        assertThrows(HttpBadRequestException.class, () -> authService.resetPassword(req));
        verify(userRepository, never()).save(any());
    }
}
