package com.ibb.yurtlar.observability;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class StructuredErrorLoggerTest {
    private ch.qos.logback.classic.Logger logger;
    private ListAppender<ILoggingEvent> appender;
    private StructuredErrorLogger structuredLogger;

    @BeforeEach
    void setUp() {
        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(StructuredErrorLogger.class);
        logger.setLevel(Level.DEBUG);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        structuredLogger = new StructuredErrorLogger(new ErrorTelemetryClassifier());
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    void validationAndExpectedBusinessErrorsDoNotLogErrorStackTraces() {
        structuredLogger.expected("VALIDATION_ERROR", HttpStatus.BAD_REQUEST,
                ErrorSource.HTTP, new IllegalArgumentException("private request body"));
        structuredLogger.expected("EMAIL_ALREADY_EXISTS", HttpStatus.CONFLICT,
                ErrorSource.HTTP, new RuntimeException("private@example.com"));

        assertThat(appender.list).extracting(ILoggingEvent::getLevel)
                .containsExactly(Level.INFO, Level.WARN);
        assertThat(appender.list).allSatisfy(event -> {
            assertThat(event.getThrowableProxy()).isNull();
            assertThat(event.getFormattedMessage())
                    .doesNotContain("private request body", "private@example.com");
        });
    }

    @Test
    void unexpectedErrorHasStableFieldsAndServerSideStackTrace() {
        RuntimeException failure = new RuntimeException("database technical detail");
        structuredLogger.unexpected("INTERNAL_SERVER_ERROR", ErrorSource.HTTP, failure);

        ILoggingEvent event = appender.list.getFirst();
        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
        assertThat(event.getThrowableProxy()).isNotNull();
        assertThat(event.getKeyValuePairs().toString())
                .contains("errorCode=\"INTERNAL_SERVER_ERROR\"",
                        "errorCategory=\"INTERNAL\"", "errorSeverity=\"CRITICAL\"",
                        "errorSource=\"HTTP\"");
    }

    @Test
    void ordinarySecurityFailureDoesNotLogCredentialOrTokenValues() {
        structuredLogger.expected("AUTHENTICATION_REQUIRED", HttpStatus.UNAUTHORIZED,
                ErrorSource.SECURITY, new RuntimeException("Bearer private-jwt-value"));

        ILoggingEvent event = appender.list.getFirst();
        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(event.getThrowableProxy()).isNull();
        assertThat(event.getFormattedMessage() + event.getKeyValuePairs())
                .doesNotContain("Bearer", "private-jwt-value");
    }
}
