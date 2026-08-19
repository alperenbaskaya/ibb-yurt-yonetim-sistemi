package com.ibb.yurtlar.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class StructuredErrorLogger {
    private static final Logger log = LoggerFactory.getLogger(StructuredErrorLogger.class);

    private final ErrorTelemetryClassifier classifier;

    public StructuredErrorLogger(ErrorTelemetryClassifier classifier) {
        this.classifier = classifier;
    }

    public void expected(String code, HttpStatus status, ErrorSource source,
                         Throwable exception) {
        ErrorTelemetry telemetry = classifier.classify(code, status, source, exception);
        LoggingEventBuilder event = switch (telemetry.severity()) {
            case INFO -> log.atInfo();
            case WARNING -> log.atWarn();
            case ERROR, CRITICAL -> log.atError();
        };
        addContext(event, code, source, telemetry, exception)
                .log("Handled application error");
    }

    public void unexpected(String code, ErrorSource source, Throwable exception) {
        ErrorTelemetry telemetry = classifier.classify(
                code, HttpStatus.INTERNAL_SERVER_ERROR, source, exception);
        addContext(log.atError(), code, source, telemetry, exception)
                .setCause(exception)
                .log("Unexpected application error");
    }

    private LoggingEventBuilder addContext(LoggingEventBuilder event, String code,
                                           ErrorSource source, ErrorTelemetry telemetry,
                                           Throwable exception) {
        return event
                .addKeyValue("component", source == ErrorSource.SECURITY ? "SECURITY" : "HTTP")
                .addKeyValue("errorCode", code)
                .addKeyValue("errorCategory", telemetry.category().name())
                .addKeyValue("errorSeverity", telemetry.severity().name())
                .addKeyValue("errorSource", source.name())
                .addKeyValue("exception", exception.getClass().getSimpleName());
    }
}
