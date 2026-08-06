package com.ibb.yurtlar.dto;

public record PendingDocumentTypeCountResponse(

        Long documentTypeId,
        String documentTypeName,
        long pendingCount

) {
}