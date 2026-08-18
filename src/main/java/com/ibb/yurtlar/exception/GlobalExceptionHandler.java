package com.ibb.yurtlar.exception;

import com.ibb.yurtlar.exception.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request
    ) {
        HttpStatus status =
                exception.getHttpStatus();

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
        log.error(
                "Beklenmeyen hata. method={}, path={}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

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