package com.ibb.yurtlar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "E-posta alanı boş olamaz.")
        @Email(message = "Geçerli bir e-posta adresi girilmelidir.")
        String email,

        @NotBlank(message = "Şifre alanı boş olamaz.")
        String password

) {
}