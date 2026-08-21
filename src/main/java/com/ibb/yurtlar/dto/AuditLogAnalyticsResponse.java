package com.ibb.yurtlar.dto;

import java.time.LocalDate;
import java.util.List;

public record AuditLogAnalyticsResponse(
        List<Bucket> byAction,
        List<Bucket> byCategory,
        List<Bucket> byDormitory,
        List<DailyBucket> activityByDay
) {
    public record Bucket(String key, long count) {}
    public record DailyBucket(LocalDate date, long count) {}
}
