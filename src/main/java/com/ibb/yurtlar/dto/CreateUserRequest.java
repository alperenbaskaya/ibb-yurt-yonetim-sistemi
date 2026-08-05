package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @NotBlank(message = "Ad alanı boş olamaz.")
        @Size(
                max = 100,
                message = "Ad en fazla 100 karakter olabilir."
        )
        String firstName,

        @NotBlank(message = "Soyad alanı boş olamaz.")
        @Size(
                max = 100,
                message = "Soyad en fazla 100 karakter olabilir."
        )
        String lastName,

        @NotBlank(message = "E-posta alanı boş olamaz.")
        @Email(message = "Geçerli bir e-posta adresi girilmelidir.")
        @Size(
                max = 150,
                message = "E-posta en fazla 150 karakter olabilir."
        )
        String email,

        @NotBlank(message = "Şifre alanı boş olamaz.")
        @Size(
                min = 8,
                max = 72,
                message = "Şifre 8 ile 72 karakter arasında olmalıdır."
        )
        String password,

        @NotNull(message = "Kullanıcı rolü zorunludur.")
        Role role,

        AdminScope adminScope,

        Long dormitoryId,

        boolean active

) {
}