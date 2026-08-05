package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.AdmissionStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminRecentAdmissionResponse(

        Long admissionId,

        Long studentId,
        String studentFirstName,
        String studentLastName,
        String identityNumber,

        String dormitoryName,

        AdmissionStatus admissionStatus,
        LocalDate admissionDate,
        LocalDateTime createdAt

) {
}