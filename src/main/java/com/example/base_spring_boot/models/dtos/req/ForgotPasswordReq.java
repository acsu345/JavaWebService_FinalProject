package com.example.base_spring_boot.models.dtos.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordReq {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
