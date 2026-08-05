package com.ibb.yurtlar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateAdmissionRequest(

        @NotNull(message = "Öğrenci ID değeri zorunludur.")
        Long studentId,

        @NotNull(message = "Yurt dönemi ID değeri zorunludur.")
        Long dormitoryTermId,

        @NotBlank(message = "Yurt adı boş olamaz.")
        @Size(
                max = 150,
                message = "Yurt adı en fazla 150 karakter olabilir."
        )
        Long dormitoryId,

        @NotNull(message = "Kabul tarihi zorunludur.")
        @PastOrPresent(
                message = "Kabul tarihi gelecekte olamaz."
        )
        LocalDate admissionDate

) {
}