package com.ibb.yurtlar.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BulkApproveAdmissionsRequest(
        @NotEmpty(message = "En az bir kabul kaydı seçilmelidir.")
        List<@NotNull(message = "Kabul kaydı ID değeri boş olamaz.") Long> admissionIds
) {
}
