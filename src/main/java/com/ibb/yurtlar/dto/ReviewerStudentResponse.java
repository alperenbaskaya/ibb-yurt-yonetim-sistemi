package com.ibb.yurtlar.dto;

public record ReviewerStudentResponse(
        Long studentId,
        String firstName,
        String lastName,
        String email,
        String identityNumber,
        String faculty,
        String department,
        String phone
) {
}
