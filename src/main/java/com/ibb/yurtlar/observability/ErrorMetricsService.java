package com.ibb.yurtlar.observability;

import com.ibb.yurtlar.exception.reason.BusinessExceptionReason;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class ErrorMetricsService {
    private static final Logger log = LoggerFactory.getLogger(ErrorMetricsService.class);
    private static final Set<String> FIXED_CODES = Set.of(
            "VALIDATION_ERROR", "INVALID_REQUEST_PARAMETER", "MISSING_REQUEST_PARAMETER",
            "INVALID_REQUEST_BODY", "AUTHENTICATION_REQUIRED", "ACCESS_DENIED",
            "INTERNAL_SERVER_ERROR", "FILE_STORAGE_ERROR", "KAFKA_PROCESSING_ERROR",
            "OUTBOX_PUBLISH_ERROR", "ELASTICSEARCH_ERROR", "UNKNOWN_ERROR");
    private static final Set<String> ALLOWED_CODES = allowedCodes();

    private final MeterRegistry registry;
    private final ErrorTelemetryClassifier classifier;

    public ErrorMetricsService(MeterRegistry registry, ErrorTelemetryClassifier classifier) {
        this.registry = registry;
        this.classifier = classifier;
    }

    public void record(String code, HttpStatus status, ErrorSource source, Throwable exception) {
        try {
            String safeCode = ALLOWED_CODES.contains(code) ? code : "UNKNOWN_ERROR";
            ErrorTelemetry telemetry = classifier.classify(safeCode, status, source, exception);
            registry.counter("yurtlar_errors_total",
                    "code", safeCode,
                    "category", telemetry.category().name(),
                    "severity", telemetry.severity().name(),
                    "status", Integer.toString(status.value()),
                    "source", source.name()).increment();
        } catch (RuntimeException metricsFailure) {
            log.debug("Error metric recording failed", metricsFailure);
        }
    }

    private static Set<String> allowedCodes() {
        Set<String> codes = new HashSet<>(FIXED_CODES);
        for (BusinessExceptionReason reason : BusinessExceptionReason.values()) {
            codes.add(reason.getCode());
        }
        return Set.copyOf(codes);
    }
}
