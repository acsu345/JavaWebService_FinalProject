package com.example.base_spring_boot.models.dtos.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserRes {
    private Long id;
    private String fullName;
    private String username;
    private String phone;
    private String email;
    private boolean enabled;
    private Set<String> roles;
}
