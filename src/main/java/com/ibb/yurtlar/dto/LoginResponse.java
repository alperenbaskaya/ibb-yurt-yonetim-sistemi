package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.Role;

public record LoginResponse(

        String accessToken,
        String tokenType,

        Long userId,
        String firstName,
        String lastName,
        String email,

        Role role,
        AdminScope adminScope,

        Long dormitoryId,
        String dormitoryName

) {
}