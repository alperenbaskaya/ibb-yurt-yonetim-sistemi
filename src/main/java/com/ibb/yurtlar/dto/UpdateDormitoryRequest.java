package com.ibb.yurtlar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateDormitoryRequest(

        @NotBlank(message = "Yurt adı boş olamaz.")
        @Size(
                max = 150,
                message = "Yurt adı en fazla 150 karakter olabilir."
        )
        String name,

        @Size(
                max = 500,
                message = "Adres en fazla 500 karakter olabilir."
        )
        String address,

        @NotNull(message = "Yurt kapasitesi zorunludur.")
        @Positive(message = "Yurt kapasitesi sıfırdan büyük olmalıdır.")
        Integer capacity,

        @NotNull(message = "Aktiflik bilgisi zorunludur.")
        Boolean active

) {
}