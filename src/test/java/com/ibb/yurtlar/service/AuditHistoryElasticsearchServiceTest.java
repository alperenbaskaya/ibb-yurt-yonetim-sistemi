package com.ibb.yurtlar.service;

import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.AuditLogRepository;
import com.ibb.yurtlar.search.audit.AuditLogSearchCriteria;
import com.ibb.yurtlar.search.audit.AuditLogSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditHistoryElasticsearchServiceTest {
    private final AuditLogRepository auditRepository = mock(AuditLogRepository.class);
    private final AppUserRepository userRepository = mock(AppUserRepository.class);
    private final AuditLogSearchService searchService = mock(AuditLogSearchService.class);
    private final DormitoryAdminAuditHistoryService service =
            new DormitoryAdminAuditHistoryService(auditRepository, userRepository, searchService);

    @Test
    void dormitorySearchUsesAuthoritativeAuthenticatedUsersDormitory() {
        when(userRepository.findByNormalizedEmail("admin@example.com"))
                .thenReturn(Optional.of(dormitoryAdmin(55L)));
        AuditLogSearchCriteria clientCriteria = new AuditLogSearchCriteria(null, null, null,
                null, null, null, 999L, null, null, null, null, 0, 20);

        service.searchOwnDormitory(clientCriteria, "admin@example.com");

        verify(searchService).search(clientCriteria, 55L);
    }

    @Test
    void existingMysqlHistoryStillReadsAuditRepository() {
        when(userRepository.findByNormalizedEmail("admin@example.com"))
                .thenReturn(Optional.of(dormitoryAdmin(55L)));
        when(auditRepository.searchDormitoryHistory(
                eq(55L), eq(AuditCategory.STUDENT_ACTIVITY), eq(""), any())).thenReturn(Page.empty());

        service.getOwnDormitoryHistory(AuditCategory.STUDENT_ACTIVITY, 0, 20, null,
                "admin@example.com");

        verify(auditRepository).searchDormitoryHistory(
                eq(55L), eq(AuditCategory.STUDENT_ACTIVITY), eq(""), any());
        verifyNoInteractions(searchService);
    }

    private AppUser dormitoryAdmin(long dormitoryId) {
        Dormitory dormitory = new Dormitory();
        ReflectionTestUtils.setField(dormitory, "id", dormitoryId);
        AppUser user = new AppUser();
        user.setActive(true);
        user.setRole(Role.ADMIN);
        user.setAdminScope(AdminScope.DORMITORY);
        user.setDormitory(dormitory);
        return user;
    }
}
