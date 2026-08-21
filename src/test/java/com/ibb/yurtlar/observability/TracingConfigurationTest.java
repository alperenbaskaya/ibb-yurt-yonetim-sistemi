package com.ibb.yurtlar.observability;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "outbox.publisher.initial-delay-ms=3600000")
@ActiveProfiles("test")
class TracingConfigurationTest {
    private final Tracer tracer;
    private final ObservationRegistry observationRegistry;
    private final MeterRegistry meterRegistry;
    private final OpenTelemetry openTelemetry;
    private final StructuredErrorLogger structuredErrorLogger;

    @Autowired
    TracingConfigurationTest(Tracer tracer, ObservationRegistry observationRegistry,
                             MeterRegistry meterRegistry, OpenTelemetry openTelemetry,
                             StructuredErrorLogger structuredErrorLogger) {
        this.tracer = tracer;
        this.observationRegistry = observationRegistry;
        this.meterRegistry = meterRegistry;
        this.openTelemetry = openTelemetry;
        this.structuredErrorLogger = structuredErrorLogger;
    }

    @Test
    void tracingAndExistingMetricsBeansLoad() {
        assertThat(tracer).isNotNull();
        assertThat(observationRegistry).isNotNull();
        assertThat(meterRegistry).isNotNull();
        assertThat(openTelemetry).isNotNull();
    }

    @Test
    void structuredErrorLogRetainsActiveTraceAndSpanIds() {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)
                LoggerFactory.getLogger(StructuredErrorLogger.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        Span span = tracer.nextSpan().name("structured-log-correlation-test").start();

        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            structuredErrorLogger.unexpected("INTERNAL_SERVER_ERROR", ErrorSource.HTTP,
                    new IllegalStateException("technical detail"));
        } finally {
            span.end();
            logger.detachAppender(appender);
        }

        ILoggingEvent event = appender.list.getFirst();
        assertThat(event.getMDCPropertyMap().get("traceId"))
                .isEqualTo(span.context().traceId());
        assertThat(event.getMDCPropertyMap().get("spanId"))
                .isEqualTo(span.context().spanId());
        assertThat(event.getFormattedMessage() + event.getKeyValuePairs())
                .doesNotContain("technical detail", "password", "Authorization");
    }
}
