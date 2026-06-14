package com.example.base_spring_boot.controllers;

import com.example.base_spring_boot.exceptions.HttpBadRequestException;
import com.example.base_spring_boot.models.dtos.req.ChangePasswordReq;
import com.example.base_spring_boot.models.dtos.req.ForgotPasswordReq;
import com.example.base_spring_boot.models.dtos.req.ResetPasswordReq;
import com.example.base_spring_boot.models.services.IAuthService;
import com.example.base_spring_boot.models.services.IJwtBlacklistService;
import com.example.base_spring_boot.models.services.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, UserController.class})
@AutoConfigureMockMvc(addFilters = false)
public class ControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IAuthService authService;

    @MockitoBean
    private IUserService userService;
    
    @MockitoBean
    private IJwtBlacklistService jwtBlacklistService;

    @Autowired
    private ObjectMapper objectMapper;

    // 1. changePassword_Return200
    @Test
    @WithMockUser
    void changePassword_Return200() throws Exception {
        ChangePasswordReq req = new ChangePasswordReq();
        req.setOldPassword("oldPass");
        req.setNewPassword("newPass");

        mockMvc.perform(post("/api/v1/users/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    // 2. forgotPassword_Return200
    @Test
    void forgotPassword_Return200() throws Exception {
        ForgotPasswordReq req = new ForgotPasswordReq();
        req.setEmail("test@gmail.com");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP has been sent to your email"));
    }

    // 3. resetPassword_Return200
    @Test
    void resetPassword_Return200() throws Exception {
        ResetPasswordReq req = new ResetPasswordReq();
        req.setEmail("test@gmail.com");
        req.setOtp("123456");
        req.setNewPassword("newPass");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successfully"));
    }

    // 4. changePassword_Return400
    @Test
    @WithMockUser
    void changePassword_Return400() throws Exception {
        ChangePasswordReq req = new ChangePasswordReq();
        req.setOldPassword("wrongPass");
        req.setNewPassword("newPass");

        doThrow(new HttpBadRequestException("Invalid old password")).when(userService).changePassword(any());

        mockMvc.perform(post("/api/v1/users/change-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid old password"));
    }

    // 5. resetPassword_Return400
    @Test
    void resetPassword_Return400() throws Exception {
        ResetPasswordReq req = new ResetPasswordReq();
        req.setEmail("test@gmail.com");
        req.setOtp("123456");
        req.setNewPassword("newPass");

        doThrow(new HttpBadRequestException("OTP has expired")).when(authService).resetPassword(any());

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("OTP has expired"));
    }
}
