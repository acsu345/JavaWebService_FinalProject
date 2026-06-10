package com.example.base_spring_boot.models.dtos.req;

import com.example.base_spring_boot.models.constants.RoleName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserUpdateReq {
    @NotBlank(message = "fullName must not be empty")
    private String fullName;
    
    @NotEmpty(message = "roles must not be empty")
    private Set<RoleName> roles;
}
