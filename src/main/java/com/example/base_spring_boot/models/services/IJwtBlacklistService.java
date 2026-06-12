package com.example.base_spring_boot.models.services;

public interface IJwtBlacklistService {
    void blacklistToken(String token);
    boolean isTokenBlacklisted(String token);
}
