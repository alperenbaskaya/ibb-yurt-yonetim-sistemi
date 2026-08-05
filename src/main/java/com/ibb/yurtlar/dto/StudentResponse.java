package com.ibb.yurtlar.dto;

import java.time.LocalDate;

public record StudentResponse(
        Long id,
        String identityNumber,
        String faculty,
        String department,
        String phone,
        LocalDate birthDate,
        Long userId,
        String firstName,
        String lastName,
        String email
) {
}