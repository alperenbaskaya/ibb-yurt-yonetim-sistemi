package com.ibb.yurtlar.config;

import com.ibb.yurtlar.exception.dto.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import com.ibb.yurtlar.observability.ErrorMetricsService;
import com.ibb.yurtlar.observability.ErrorSource;
import com.ibb.yurtlar.observability.StructuredErrorLogger;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final ErrorMetricsService errorMetricsService;
    private final StructuredErrorLogger structuredErrorLogger;

    public CustomAuthenticationEntryPoint(
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
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authenticationException
    ) throws IOException, ServletException {
        errorMetricsService.record("AUTHENTICATION_REQUIRED", HttpStatus.UNAUTHORIZED,
                ErrorSource.SECURITY, authenticationException);
        structuredErrorLogger.expected("AUTHENTICATION_REQUIRED", HttpStatus.UNAUTHORIZED,
                ErrorSource.SECURITY, authenticationException);

        ErrorResponse errorResponse =
                new ErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.UNAUTHORIZED.value(),
                        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        "AUTHENTICATION_REQUIRED",
                        "Kimlik doğrulaması gereklidir. "
                                + "Geçerli bir erişim tokenı gönderiniz.",
                        request.getRequestURI(),
                        null
                );

        response.setStatus(
                HttpStatus.UNAUTHORIZED.value()
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
