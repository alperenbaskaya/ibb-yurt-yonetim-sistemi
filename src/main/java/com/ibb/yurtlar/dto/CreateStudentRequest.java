package com.ibb.yurtlar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateStudentRequest(

        @NotBlank(message = "TC kimlik numarası boş olamaz.")
        @Pattern(regexp = "\\d{11}", message = "TC kimlik numarası 11 rakamdan oluşmalıdır.")
        String identityNumber,

        @NotBlank(message = "Fakülte alanı boş olamaz.")
        @Size(
                max = 150,
                message = "Fakülte en fazla 150 karakter olabilir."
        )
        String faculty,

        @NotBlank(message = "Bölüm alanı boş olamaz.")
        @Size(
                max = 150,
                message = "Bölüm en fazla 150 karakter olabilir."
        )
        String department,

        @NotBlank(message = "Telefon numarası boş olamaz.")
        @Size(
                max = 10,
                message = "Telefon numarası en fazla 10 karakter olabilir. Başında 'SIFIR' olmadan yazınız. "
        )
        String phone,

        @NotNull(message = "Doğum tarihi zorunludur.")
        @Past(message = "Doğum tarihi geçmiş bir tarih olmalıdır.")
        LocalDate birthDate,

        @NotNull(message = "Kullanıcı ID değeri zorunludur.")
        Long userId
) {
}