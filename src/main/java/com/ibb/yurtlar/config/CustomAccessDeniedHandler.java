package com.ibb.yurtlar.config;

import com.ibb.yurtlar.exception.dto.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import com.ibb.yurtlar.observability.ErrorMetricsService;
import com.ibb.yurtlar.observability.ErrorSource;
import com.ibb.yurtlar.observability.StructuredErrorLogger;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAccessDeniedHandler
        implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final ErrorMetricsService errorMetricsService;
    private final StructuredErrorLogger structuredErrorLogger;

    public CustomAccessDeniedHandler(
            ObjectMapper objectMapper,
            ErrorMetricsService errorMetricsService,
            StructuredErrorLogger structuredErrorLogger
    ) {
        this.objectMapper =
                objectMapper;
        this.errorMetricsService = errorMetricsService;
        this.structuredErrorLogger = structuredErrorLogger;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        errorMetricsService.record("ACCESS_DENIED", HttpStatus.FORBIDDEN,
                ErrorSource.SECURITY, accessDeniedException);
        structuredErrorLogger.expected("ACCESS_DENIED", HttpStatus.FORBIDDEN,
                ErrorSource.SECURITY, accessDeniedException);

        ErrorResponse errorResponse =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.FORBIDDEN.value(),
                        HttpStatus.FORBIDDEN.getReasonPhrase(),
                        "ACCESS_DENIED",
                        "Bu işlem için yetkiniz bulunmamaktadır.",
                        request.getRequestURI(),
                        null
                );

        response.setStatus(
                HttpStatus.FORBIDDEN.value()
        );

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        objectMapper.writeValue(
                response.getOutputStream(),
                errorResponse
        );
    }
}
