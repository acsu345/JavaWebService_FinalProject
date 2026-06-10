package com.example.base_spring_boot.models.services.impl;

import com.example.base_spring_boot.exceptions.HttpNotFoundException;
import com.example.base_spring_boot.models.dtos.req.UserUpdateReq;
import com.example.base_spring_boot.models.dtos.res.UserRes;
import com.example.base_spring_boot.models.entities.Role;
import com.example.base_spring_boot.models.entities.User;
import com.example.base_spring_boot.models.repositories.IUserRepository;
import com.example.base_spring_boot.models.services.IRoleService;
import com.example.base_spring_boot.models.services.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {
    private final IUserRepository userRepository;
    private final IRoleService roleService;

    @Override
    public Page<UserRes> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToUserRes);
    }

    @Override
    public Page<UserRes> search(String keyword, Pageable pageable) {
        return userRepository.searchUsers(keyword, pageable).map(this::mapToUserRes);
    }

    @Override
    public UserRes findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new HttpNotFoundException("User not found with id: " + id));
        return mapToUserRes(user);
    }

    @Override
    @Transactional
    public UserRes update(Long id, UserUpdateReq req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new HttpNotFoundException("User not found with id: " + id));
        
        user.setFullName(req.getFullName());
        
        Set<Role> roles = req.getRoles().stream()
                .map(roleService::findByRoleName)
                .collect(Collectors.toSet());
        user.setRoles(roles);
        
        return mapToUserRes(userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new HttpNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserRes mapToUserRes(User user) {
        return UserRes.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .roles(user.getRoles().stream()
                        .map(role -> role.getRoleName().toString())
                        .collect(Collectors.toSet()))
                .build();
    }
}
