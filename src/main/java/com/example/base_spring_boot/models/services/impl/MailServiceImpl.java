package com.example.base_spring_boot.models.services.impl;

import com.example.base_spring_boot.models.services.IMailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements IMailService {
    private final JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Password Reset OTP");
            message.setText("Your OTP for password reset is: " + otp + ". It will expire in 10 minutes.");
            mailSender.send(message);
            log.info("OTP email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}. OTP is: {}", to, e.getMessage(), otp);
            // Không throw exception để tránh lỗi 500, người dùng có thể lấy OTP từ log console để test
        }
    }
}
