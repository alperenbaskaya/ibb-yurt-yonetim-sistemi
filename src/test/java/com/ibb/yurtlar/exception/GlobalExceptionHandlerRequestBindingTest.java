package com.ibb.yurtlar.exception;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.*;

import com.ibb.yurtlar.exception.BusinessException;

import com.ibb.yurtlar.controller.GlobalAuditHistoryController;
import com.ibb.yurtlar.service.GlobalAuditHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ibb.yurtlar.observability.ErrorMetricsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import com.ibb.yurtlar.observability.ErrorSource;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerRequestBindingTest {

    @Mock GlobalAuditHistoryService historyService;
    @Mock ErrorMetricsService errorMetricsService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new GlobalAuditHistoryController(historyService),
                        new FailureProbeController()
                )
                .setControllerAdvice(new GlobalExceptionHandler(errorMetricsService))
                .build();
    }

    @Test
    void malformedPageReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/global-history/system-management")
                        .param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Geçersiz istek parametresi: page"));
        verify(errorMetricsService).record(eq("INVALID_REQUEST_PARAMETER"),
                eq(HttpStatus.BAD_REQUEST), eq(ErrorSource.HTTP), any());
    }

    @Test
    void malformedSizeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/global-history/system-management")
                        .param("size", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Geçersiz istek parametresi: size"));
    }

    @Test
    void missingRequiredDormitoryIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/global-history/dormitory-operations")
                        .param("category", "STUDENT_ACTIVITY"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Zorunlu istek parametresi eksik: dormitoryId"));
    }

    @Test
    void malformedDormitoryIdReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/global-history/dormitory-operations")
                        .param("dormitoryId", "abc")
                        .param("category", "STUDENT_ACTIVITY"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Geçersiz istek parametresi: dormitoryId"));
    }

    @Test
    void validButUnsupportedHistoryCategoryKeepsExistingBadRequest() throws Exception {
        mockMvc.perform(get("/test/invalid-history"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Bu kategori yurt operasyon geçmişi için desteklenmiyor."));
        verify(errorMetricsService).record(eq("INVALID_AUDIT_HISTORY_REQUEST"),
                eq(HttpStatus.BAD_REQUEST), eq(ErrorSource.HTTP), any());
    }

    @Test
    void unexpectedServerExceptionStillReturnsInternalServerError() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message")
                        .value("Beklenmeyen bir hata oluştu."));
        verify(errorMetricsService).record(eq("INTERNAL_SERVER_ERROR"),
                eq(HttpStatus.INTERNAL_SERVER_ERROR), eq(ErrorSource.HTTP), any());
    }

    @RestController
    static class FailureProbeController {

        @GetMapping("/test/invalid-history")
        void invalidHistory() {
            throw new BusinessException(INVALID_AUDIT_HISTORY_REQUEST,
                    "Bu kategori yurt operasyon geçmişi için desteklenmiyor."
            );
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new NullPointerException("internal detail");
        }
    }
}
