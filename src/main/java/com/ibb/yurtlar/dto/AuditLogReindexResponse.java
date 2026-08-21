package com.ibb.yurtlar.dto;

public record AuditLogReindexResponse(
        long highWaterMark,
        long scanned,
        long indexed,
        long lastProcessedId
) {
}
