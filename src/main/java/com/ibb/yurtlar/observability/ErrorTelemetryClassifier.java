package com.ibb.yurtlar.observability;

import com.ibb.yurtlar.exception.FileStorageException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ErrorTelemetryClassifier {

    public ErrorTelemetry classify(String code, HttpStatus status,
                                   ErrorSource source, Throwable exception) {
        if (source == ErrorSource.KAFKA) {
            return new ErrorTelemetry(ErrorCategory.KAFKA, ErrorSeverity.ERROR);
        }
        if (source == ErrorSource.OUTBOX) {
            return new ErrorTelemetry(ErrorCategory.OUTBOX, ErrorSeverity.ERROR);
        }
        if (source == ErrorSource.ELASTICSEARCH || "AUDIT_SEARCH_UNAVAILABLE".equals(code)) {
            return new ErrorTelemetry(ErrorCategory.ELASTICSEARCH, ErrorSeverity.ERROR);
        }
        if (source == ErrorSource.STORAGE || exception instanceof FileStorageException) {
            return new ErrorTelemetry(ErrorCategory.FILE_STORAGE, ErrorSeverity.ERROR);
        }
        if (exception instanceof DataAccessException) {
            return new ErrorTelemetry(ErrorCategory.DATABASE, ErrorSeverity.CRITICAL);
        }
        if (status == HttpStatus.UNAUTHORIZED) {
            return new ErrorTelemetry(ErrorCategory.AUTHENTICATION, ErrorSeverity.INFO);
        }
        if (status == HttpStatus.FORBIDDEN) {
            return new ErrorTelemetry(ErrorCategory.AUTHORIZATION, ErrorSeverity.WARNING);
        }
        if (status == HttpStatus.BAD_REQUEST && isValidationCode(code)) {
            return new ErrorTelemetry(ErrorCategory.VALIDATION, ErrorSeverity.INFO);
        }
        if (status == HttpStatus.NOT_FOUND) {
            return new ErrorTelemetry(ErrorCategory.BUSINESS_RULE, ErrorSeverity.INFO);
        }
        if (status == HttpStatus.CONFLICT || status == HttpStatus.BAD_REQUEST) {
            return new ErrorTelemetry(ErrorCategory.BUSINESS_RULE, ErrorSeverity.WARNING);
        }
        return new ErrorTelemetry(ErrorCategory.INTERNAL, ErrorSeverity.CRITICAL);
    }

    private boolean isValidationCode(String code) {
        return "VALIDATION_ERROR".equals(code)
                || "INVALID_REQUEST_PARAMETER".equals(code)
                || "MISSING_REQUEST_PARAMETER".equals(code)
                || "INVALID_REQUEST_BODY".equals(code);
    }
}
