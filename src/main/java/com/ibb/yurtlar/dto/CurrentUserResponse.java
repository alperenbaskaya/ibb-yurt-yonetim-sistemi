package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.Role;

public record CurrentUserResponse(

        Long userId,
        String firstName,
        String lastName,
        String email,

        Role role,
        AdminScope adminScope,
        boolean active,

        Long dormitoryId,
        String dormitoryName

) {
}
