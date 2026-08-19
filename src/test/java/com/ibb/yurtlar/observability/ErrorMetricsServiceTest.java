package com.ibb.yurtlar.observability;

import com.ibb.yurtlar.exception.BusinessException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.EMAIL_ALREADY_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;

class ErrorMetricsServiceTest {
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final ErrorMetricsService service =
            new ErrorMetricsService(registry, new ErrorTelemetryClassifier());

    @Test
    void businessExceptionUsesStableCodeAndClassificationTags() {
        BusinessException exception = new BusinessException(EMAIL_ALREADY_EXISTS, "private@example.com");
        service.record(exception.getCode(), exception.getHttpStatus(), ErrorSource.HTTP, exception);

        assertThat(registry.get("yurtlar_errors_total")
                .tags("code", "EMAIL_ALREADY_EXISTS", "category", "BUSINESS_RULE",
                        "severity", "WARNING", "status", "409", "source", "HTTP")
                .counter().count()).isEqualTo(1);
    }

    @Test
    void arbitraryValuesCannotBecomeMetricTags() {
        service.record("eventId-123-user@example.com", org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorSource.HTTP, new RuntimeException("documentId=999 secret message"));

        var meter = registry.get("yurtlar_errors_total").counter().getId();
        assertThat(meter.getTag("code")).isEqualTo("UNKNOWN_ERROR");
        assertThat(meter.getTags().toString())
                .doesNotContain("123", "example.com", "documentId", "secret message");
    }
}
