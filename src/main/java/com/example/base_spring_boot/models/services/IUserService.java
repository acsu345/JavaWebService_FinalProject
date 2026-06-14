package com.example.base_spring_boot.models.services;

import com.example.base_spring_boot.models.dtos.req.ChangePasswordReq;
import com.example.base_spring_boot.models.dtos.req.UserUpdateReq;
import com.example.base_spring_boot.models.dtos.res.UserRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {
    Page<UserRes> findAll(Pageable pageable);
    Page<UserRes> search(String keyword, Pageable pageable);
    UserRes findById(Long id);
    UserRes update(Long id, UserUpdateReq req);
    void changePassword(ChangePasswordReq req);
    void delete(Long id);
}
