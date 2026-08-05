package com.ibb.yurtlar.dto;

public record DocumentCompletionResponse(
        Long admissionId,
        int totalRequiredDocuments,
        int approvedRequiredDocuments,
        boolean completed
) {
}