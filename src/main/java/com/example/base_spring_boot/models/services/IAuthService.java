package com.example.base_spring_boot.models.services;

import com.example.base_spring_boot.models.dtos.req.ForgotPasswordReq;
import com.example.base_spring_boot.models.dtos.req.LoginReq;
import com.example.base_spring_boot.models.dtos.req.RegisterReq;
import com.example.base_spring_boot.models.dtos.req.ResetPasswordReq;
import com.example.base_spring_boot.models.dtos.res.JwtRes;

public interface IAuthService
{

    void register(RegisterReq req);

    JwtRes login(LoginReq req);
    JwtRes refreshToken(String refreshToken);
    void forgotPassword(ForgotPasswordReq req);
    void resetPassword(ResetPasswordReq req);
}
