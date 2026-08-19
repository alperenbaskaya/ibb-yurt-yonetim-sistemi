package com.ibb.yurtlar.observability;

import com.ibb.yurtlar.exception.FileStorageException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorTelemetryClassifierTest {
    private final ErrorTelemetryClassifier classifier = new ErrorTelemetryClassifier();

    @Test
    void classificationsAreStableAndBounded() {
        assertThat(classifier.classify("VALIDATION_ERROR", HttpStatus.BAD_REQUEST,
                ErrorSource.HTTP, null)).isEqualTo(
                new ErrorTelemetry(ErrorCategory.VALIDATION, ErrorSeverity.INFO));
        assertThat(classifier.classify("AUTHENTICATION_REQUIRED", HttpStatus.UNAUTHORIZED,
                ErrorSource.SECURITY, null)).isEqualTo(
                new ErrorTelemetry(ErrorCategory.AUTHENTICATION, ErrorSeverity.INFO));
        assertThat(classifier.classify("ACCESS_DENIED", HttpStatus.FORBIDDEN,
                ErrorSource.SECURITY, null)).isEqualTo(
                new ErrorTelemetry(ErrorCategory.AUTHORIZATION, ErrorSeverity.WARNING));
        assertThat(classifier.classify("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorSource.HTTP, new NullPointerException())).isEqualTo(
                new ErrorTelemetry(ErrorCategory.INTERNAL, ErrorSeverity.CRITICAL));
        assertThat(classifier.classify("FILE_STORAGE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorSource.HTTP, new FileStorageException("disk detail", null))).isEqualTo(
                new ErrorTelemetry(ErrorCategory.FILE_STORAGE, ErrorSeverity.ERROR));
    }
}
