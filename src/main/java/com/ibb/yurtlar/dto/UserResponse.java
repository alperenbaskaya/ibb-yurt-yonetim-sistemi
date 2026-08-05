package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.Role;

public record UserResponse(

        Long id,
        String firstName,
        String lastName,
        String email,
        Role role,
        boolean active,

        AdminScope adminScope,

        Long dormitoryId,
        String dormitoryName

) {
}