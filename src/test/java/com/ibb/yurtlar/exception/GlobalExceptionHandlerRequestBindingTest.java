package com.ibb.yurtlar.exception;

import com.ibb.yurtlar.controller.GlobalAuditHistoryController;
import com.ibb.yurtlar.service.GlobalAuditHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerRequestBindingTest {

    @Mock GlobalAuditHistoryService historyService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new GlobalAuditHistoryController(historyService),
                        new FailureProbeController()
                )
                .setControllerAdvice(new GlobalExceptionHandler())
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
    }

    @Test
    void unexpectedServerExceptionStillReturnsInternalServerError() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message")
                        .value("Beklenmeyen bir hata oluştu."));
    }

    @RestController
    static class FailureProbeController {

        @GetMapping("/test/invalid-history")
        void invalidHistory() {
            throw new InvalidAuditHistoryRequestException(
                    "Bu kategori yurt operasyon geçmişi için desteklenmiyor."
            );
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new NullPointerException("internal detail");
        }
    }
}
