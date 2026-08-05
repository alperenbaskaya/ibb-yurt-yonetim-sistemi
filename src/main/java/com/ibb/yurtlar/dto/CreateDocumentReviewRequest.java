package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.DocumentReviewDecision;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDocumentReviewRequest(

        @NotNull(message = "Öğrenci belgesi ID değeri zorunludur.")
        Long studentDocumentId,

        @NotNull(message = "Görevli kullanıcı ID değeri zorunludur.")
        Long reviewerUserId,

        @NotNull(message = "Değerlendirme kararı zorunludur.")
        DocumentReviewDecision decision,

        @Size(
                max = 1000,
                message = "Değerlendirme açıklaması en fazla 1000 karakter olabilir."
        )
        String comment

) {
}