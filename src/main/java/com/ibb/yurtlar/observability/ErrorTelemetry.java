package com.ibb.yurtlar.observability;

public record ErrorTelemetry(ErrorCategory category, ErrorSeverity severity) {
}
