package com.ibb.yurtlar.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record CreateAdmissionRequest(

        @NotNull(message = "Öğrenci ID değeri zorunludur.")
        Long studentId,

        @NotNull(message = "Yurt dönemi ID değeri zorunludur.")
        Long dormitoryTermId,

        @NotNull(message = "Yurt ID değeri zorunludur.")
        Long dormitoryId,

        @NotNull(message = "Kabul tarihi zorunludur.")
        @PastOrPresent(
                message = "Kabul tarihi gelecekte olamaz."
        )
        LocalDate admissionDate

) {
}