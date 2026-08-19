package com.ibb.yurtlar.exception;

import com.ibb.yurtlar.exception.dto.ErrorResponse;
import com.ibb.yurtlar.observability.ErrorMetricsService;
import com.ibb.yurtlar.observability.ErrorSource;
import com.ibb.yurtlar.observability.StructuredErrorLogger;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final ErrorMetricsService errorMetricsService;
    private final StructuredErrorLogger structuredErrorLogger;

    public GlobalExceptionHandler(ErrorMetricsService errorMetricsService,
                                  StructuredErrorLogger structuredErrorLogger) {
        this.errorMetricsService = errorMetricsService;
        this.structuredErrorLogger = structuredErrorLogger;
    }


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        HttpStatus status =
                exception.getHttpStatus();
        errorMetricsService.record(exception.getCode(), status, ErrorSource.HTTP, exception);
        structuredErrorLogger.expected(exception.getCode(), status, ErrorSource.HTTP, exception);

        ErrorResponse response =
                createErrorResponse(
                        status,
                        exception.getCode(),
                        exception.getMessage(),
                        request.getRequestURI(),
                        null
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        errorMetricsService.record("VALIDATION_ERROR", HttpStatus.BAD_REQUEST,
                ErrorSource.HTTP, exception);
        structuredErrorLogger.expected("VALIDATION_ERROR", HttpStatus.BAD_REQUEST,
                ErrorSource.HTTP, exception);
        Map<String, String> validationErrors =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(fieldError ->
                        validationErrors.put(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        )
                );

        ErrorResponse response =
                createErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        "VALIDATION_ERROR",
                        "Gönderilen bilgiler doğrulanamadı.",
                        request.getRequestURI(),
                        validationErrors
                );

        return ResponseEntity
                .badRequest()
                .body(response);
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        errorMetricsService.record("INVALID_REQUEST_PARAMETER", HttpStatus.BAD_REQUEST,
                ErrorSource.HTTP, exception);
        structuredErrorLogger.expected("INVALID_REQUEST_PARAMETER", HttpStatus.BAD_REQUEST,
                ErrorSource.HTTP, exception);
        return ResponseEntity
                .badRequest()
                .body(
                        createErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                "INVALID_REQUEST_PARAMETER",
                                "Geçersiz istek parametresi: "
                                        + exception.getName(),
                                request.getRequestURI(),
                                null
                        )
                );
    }


    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        errorMetricsService.record("MISSING_REQUEST_PARAMETER", HttpStatus.BAD_REQUEST,
                ErrorSource.HTTP, exception);
        structuredErrorLogger.expected("MISSING_REQUEST_PARAMETER", HttpStatus.BAD_REQUEST,
                ErrorSource.HTTP, exception);
        return ResponseEntity
                .badRequest()
                .body(
                        createErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                "MISSING_REQUEST_PARAMETER",
                                "Zorunlu istek parametresi eksik: "
                                        + exception.getParameterName(),
                                request.getRequestURI(),
                                null
                        )
                );
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        errorMetricsService.record("INVALID_REQUEST_BODY", HttpStatus.BAD_REQUEST,
                ErrorSource.HTTP, exception);
        structuredErrorLogger.expected("INVALID_REQUEST_BODY", HttpStatus.BAD_REQUEST,
                ErrorSource.HTTP, exception);
        return ResponseEntity
                .badRequest()
                .body(
                        createErrorResponse(
                                HttpStatus.BAD_REQUEST,
                                "INVALID_REQUEST_BODY",
                                "İstek gövdesi okunamadı. "
                                        + "JSON yapısını ve gönderilen alan değerlerini kontrol edin.",
                                request.getRequestURI(),
                                null
                        )
                );
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        errorMetricsService.record("ACCESS_DENIED", HttpStatus.FORBIDDEN,
                ErrorSource.HTTP, exception);
        structuredErrorLogger.expected("ACCESS_DENIED", HttpStatus.FORBIDDEN,
                ErrorSource.HTTP, exception);
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        createErrorResponse(
                                HttpStatus.FORBIDDEN,
                                "ACCESS_DENIED",
                                "Bu işlem için yetkiniz bulunmamaktadır.",
                                request.getRequestURI(),
                                null
                        )
                );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        errorMetricsService.record("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorSource.HTTP, exception);
        structuredErrorLogger.unexpected("INTERNAL_SERVER_ERROR", ErrorSource.HTTP, exception);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        createErrorResponse(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "INTERNAL_SERVER_ERROR",
                                "Beklenmeyen bir hata oluştu.",
                                request.getRequestURI(),
                                null
                        )
                );
    }


    private ErrorResponse createErrorResponse(
            HttpStatus status,
            String code,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {
        return new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                path,
                validationErrors
        );
    }
}
