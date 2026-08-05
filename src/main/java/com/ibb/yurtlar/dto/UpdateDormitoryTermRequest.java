package com.ibb.yurtlar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateDormitoryTermRequest(

        @NotBlank(message = "Dönem adı boş olamaz.")
        @Size(max = 20, message = "Dönem adı en fazla 20 karakter olabilir.")
        String name,

        @NotNull(message = "Dönem başlangıç tarihi zorunludur.")
        LocalDate startDate,

        @NotNull(message = "Dönem bitiş tarihi zorunludur.")
        LocalDate endDate,

        @NotNull(message = "Belge yükleme başlangıç tarihi zorunludur.")
        LocalDate documentUploadStartDate,

        @NotNull(message = "Belge yükleme bitiş tarihi zorunludur.")
        LocalDate documentUploadEndDate,

        boolean active
) {
}