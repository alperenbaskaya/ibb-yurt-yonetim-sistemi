package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.AdmissionStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAdmissionStatusRequest(

        @NotNull(message = "Admission durumu zorunludur.")
        AdmissionStatus status

) {
}