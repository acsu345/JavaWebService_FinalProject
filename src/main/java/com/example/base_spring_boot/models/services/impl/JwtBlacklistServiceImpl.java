package com.example.base_spring_boot.models.services.impl;

import com.example.base_spring_boot.models.entities.TokenBlacklist;
import com.example.base_spring_boot.models.repositories.ITokenBlacklistRepository;
import com.example.base_spring_boot.models.services.IJwtBlacklistService;
import com.example.base_spring_boot.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtBlacklistServiceImpl implements IJwtBlacklistService {
    private final ITokenBlacklistRepository tokenBlacklistRepository;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public void blacklistToken(String token) {
        if (tokenBlacklistRepository.existsByToken(token)) {
            return;
        }

        Date expirationDate = jwtUtils.extractExpiration(token);
        LocalDateTime expiredAt = expirationDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        TokenBlacklist blacklist = TokenBlacklist.builder()
                .token(token)
                .expiredAt(expiredAt)
                .build();
        
        tokenBlacklistRepository.save(blacklist);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }
}
