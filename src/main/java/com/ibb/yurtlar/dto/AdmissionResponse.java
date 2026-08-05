package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.AdmissionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdmissionResponse(

        Long id,

        Long studentId,
        String studentFirstName,
        String studentLastName,
        String identityNumber,

        Long dormitoryTermId,
        String dormitoryTermName,

        Long dormitoryId,
        String dormitoryName,

        LocalDate admissionDate,
        AdmissionStatus status,

        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}