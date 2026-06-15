package com.example.base_spring_boot.models.services.impl;

import com.example.base_spring_boot.exceptions.HttpBadRequestException;
import com.example.base_spring_boot.exceptions.HttpNotFoundException;
import com.example.base_spring_boot.models.constants.RoleName;
import com.example.base_spring_boot.models.dtos.req.ForgotPasswordReq;
import com.example.base_spring_boot.models.dtos.req.LoginReq;
import com.example.base_spring_boot.models.dtos.req.RegisterReq;
import com.example.base_spring_boot.models.dtos.req.ResetPasswordReq;
import com.example.base_spring_boot.models.dtos.res.JwtRes;
import com.example.base_spring_boot.models.entities.PasswordOtp;
import com.example.base_spring_boot.models.entities.Role;
import com.example.base_spring_boot.models.entities.User;
import com.example.base_spring_boot.models.repositories.IPasswordOtpRepository;
import com.example.base_spring_boot.models.repositories.IUserRepository;
import com.example.base_spring_boot.models.services.IAuthService;
import com.example.base_spring_boot.models.services.IMailService;
import com.example.base_spring_boot.models.services.IRoleService;
import com.example.base_spring_boot.security.jwt.JwtUtils;
import com.example.base_spring_boot.security.principal.MyUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService
{
    private final IRoleService roleService;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final IMailService mailService;
    private final IPasswordOtpRepository passwordOtpRepository;

    @Override
    public void register(RegisterReq req)
    {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new HttpBadRequestException("Username is already taken");
        }

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new HttpBadRequestException("Email is already registered");
        }

        Set<Role> roles = new HashSet<>();
        roles.add(roleService.findByRoleName(RoleName.ROLE_CUSTOMER));
        User user = User.builder()
                .fullName(req.getFullName())
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .email(req.getEmail())
                .roles(roles)
                .build();
        userRepository.save(user);
    }

    @Override
    public JwtRes login(LoginReq req)
    {
        Authentication authentication;
        try
        {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));
        }
        catch (AuthenticationException e)
        {
            throw new HttpBadRequestException("Username or password is incorrect");
        }

        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String accessToken = jwtUtils.generateToken(user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return JwtRes.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .roles(userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()))
                .build();
    }

    @Override
    public JwtRes refreshToken(String refreshToken)
    {
        String username = jwtUtils.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new HttpBadRequestException("Invalid refresh token"));

        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken))
        {
            throw new HttpBadRequestException("Invalid refresh token");
        }

        // Tạo bộ đôi token mới (Rotation)
        String newAccessToken = jwtUtils.generateToken(username);
        String newRefreshToken = jwtUtils.generateRefreshToken(username);

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return JwtRes.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .roles(user.getRoles().stream().map(role -> role.getRoleName().name()).collect(Collectors.toSet()))
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordReq req) {
        if (!userRepository.existsByEmail(req.getEmail())) {
            throw new HttpNotFoundException("Email not found");
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        passwordOtpRepository.deleteByEmail(req.getEmail());
        
        PasswordOtp passwordOtp = PasswordOtp.builder()
                .email(req.getEmail())
                .otp(otp)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(10))
                .build();
        
        passwordOtpRepository.save(passwordOtp);
        log.info("--- FORGOT PASSWORD OTP FOR {}: {} ---", req.getEmail(), otp);
        mailService.sendOtpEmail(req.getEmail(), otp);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordReq req) {
        PasswordOtp passwordOtp = passwordOtpRepository.findByEmailAndOtp(req.getEmail(), req.getOtp())
                .orElseThrow(() -> new HttpBadRequestException("Invalid OTP"));

        if (passwordOtp.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new HttpBadRequestException("OTP has expired");
        }

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new HttpNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        
        passwordOtpRepository.delete(passwordOtp);
    }


}