package com.example.base_spring_boot.models.services;

public interface IMailService {
    void sendOtpEmail(String to, String otp);
}
