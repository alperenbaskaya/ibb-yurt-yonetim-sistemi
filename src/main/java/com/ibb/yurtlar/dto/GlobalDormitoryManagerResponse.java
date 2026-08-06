package com.ibb.yurtlar.dto;

public record GlobalDormitoryManagerResponse(

        Long userId,
        String firstName,
        String lastName,
        String email,
        boolean active

) {
}