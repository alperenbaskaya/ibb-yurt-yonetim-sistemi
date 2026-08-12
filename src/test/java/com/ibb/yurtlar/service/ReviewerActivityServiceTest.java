package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.AuditLogResponse;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.AuditLog;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.AuditEntityType;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.exception.InvalidUserConfigurationException;
import com.ibb.yurtlar.exception.UserIsNotReviewerException;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.domain.Pageable;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewerActivityServiceTest {
    @Mock AuditLogRepository auditLogRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock ReviewerRecentActivityProjection projection;

    @Test
    void redisHitReturnsProjectionWithoutRecentAuditQuery() {
        ReviewerActivityService service = service();
        AuditLogResponse projected = response(1L);
        when(appUserRepository.findByNormalizedEmail("reviewer@example.com"))
                .thenReturn(Optional.of(reviewer(10L)));
        when(projection.readRecent(10L, 5)).thenReturn(Optional.of(List.of(projected)));

        assertEquals(List.of(projected), service.getRecentActivity("reviewer@example.com"));

        verify(auditLogRepository, never()).findRecentByDormitoryCategoryAndActions(
                any(), any(), anyCollection(), any(Pageable.class));
    }

    @Test
    void redisMissFallsBackToMysqlAndWarmsProjection() {
        ReviewerActivityService service = service();
        AuditLog auditLog = auditLog(2L);
        when(appUserRepository.findByNormalizedEmail("reviewer@example.com"))
                .thenReturn(Optional.of(reviewer(10L)));
        when(projection.readRecent(10L, 5)).thenReturn(Optional.empty());
        when(auditLogRepository.findRecentByDormitoryCategoryAndActions(
                eq(10L), eq(AuditCategory.STUDENT_ACTIVITY), anyCollection(), any(Pageable.class)))
                .thenReturn(List.of(auditLog));

        List<AuditLogResponse> result = service.getRecentActivity("reviewer@example.com");

        assertEquals(List.of(response(2L)), result);
        verify(projection).mergeAndInitialize(10L, result);
    }

    @Test
    void initializedEmptyProjectionAvoidsMysqlAfterFirstFallback() {
        ReviewerActivityService service = service();
        when(appUserRepository.findByNormalizedEmail("reviewer@example.com"))
                .thenReturn(Optional.of(reviewer(10L)));
        when(projection.readRecent(10L, 5))
                .thenReturn(Optional.empty(), Optional.of(List.of()));
        when(auditLogRepository.findRecentByDormitoryCategoryAndActions(
                eq(10L), eq(AuditCategory.STUDENT_ACTIVITY), anyCollection(), any(Pageable.class)))
                .thenReturn(List.of());

        assertEquals(List.of(), service.getRecentActivity("reviewer@example.com"));
        assertEquals(List.of(), service.getRecentActivity("reviewer@example.com"));

        verify(auditLogRepository).findRecentByDormitoryCategoryAndActions(
                eq(10L), eq(AuditCategory.STUDENT_ACTIVITY), anyCollection(), any(Pageable.class));
        verify(projection).mergeAndInitialize(10L, List.of());
    }

    @Test
    void redisFailureFallsBackToMysqlResult() {
        StringRedisTemplate unavailableRedis = mock(StringRedisTemplate.class);
        when(unavailableRedis.opsForZSet())
                .thenThrow(new RedisConnectionFailureException("Redis unavailable"));
        ReviewerRecentActivityProjection failingProjection =
                new ReviewerRecentActivityProjection(unavailableRedis, mock(ObjectMapper.class));
        ReviewerActivityService service = new ReviewerActivityService(
                auditLogRepository, appUserRepository, failingProjection);
        AuditLog auditLog = auditLog(3L);
        when(appUserRepository.findByNormalizedEmail("reviewer@example.com"))
                .thenReturn(Optional.of(reviewer(10L)));
        when(auditLogRepository.findRecentByDormitoryCategoryAndActions(
                eq(10L), eq(AuditCategory.STUDENT_ACTIVITY), anyCollection(), any(Pageable.class)))
                .thenReturn(List.of(auditLog));

        assertEquals(List.of(response(3L)), service.getRecentActivity("reviewer@example.com"));
    }

    @Test
    void wrongRoleRemainsRejectedBeforeProjectionAccess() {
        ReviewerActivityService service = service();
        AppUser student = reviewer(10L);
        student.setRole(Role.STUDENT);
        student.setId(8L);
        when(appUserRepository.findByNormalizedEmail("student@example.com"))
                .thenReturn(Optional.of(student));

        assertThrows(UserIsNotReviewerException.class,
                () -> service.getRecentActivity("student@example.com"));
        verify(projection, never()).readRecent(any(), any(Integer.class));
    }

    @Test
    void reviewerWithoutDormitoryRemainsRejectedBeforeProjectionAccess() {
        ReviewerActivityService service = service();
        when(appUserRepository.findByNormalizedEmail("reviewer@example.com"))
                .thenReturn(Optional.of(reviewer(null)));

        assertThrows(InvalidUserConfigurationException.class,
                () -> service.getRecentActivity("reviewer@example.com"));
        verify(projection, never()).readRecent(any(), any(Integer.class));
    }

    private ReviewerActivityService service() {
        return new ReviewerActivityService(auditLogRepository, appUserRepository, projection);
    }

    private AppUser reviewer(Long dormitoryId) {
        AppUser reviewer = new AppUser();
        reviewer.setId(7L);
        reviewer.setRole(Role.REVIEWER);
        reviewer.setActive(true);
        if (dormitoryId != null) {
            Dormitory dormitory = new Dormitory();
            dormitory.setId(dormitoryId);
            reviewer.setDormitory(dormitory);
        }
        return reviewer;
    }

    private AuditLog auditLog(Long id) {
        AuditLog auditLog = mock(AuditLog.class);
        AuditLogResponse response = response(id);
        when(auditLog.getId()).thenReturn(response.id());
        when(auditLog.getActorUserId()).thenReturn(response.actorUserId());
        when(auditLog.getActorName()).thenReturn(response.actorName());
        when(auditLog.getActorRole()).thenReturn(response.actorRole());
        when(auditLog.getSubjectStudentId()).thenReturn(response.subjectStudentId());
        when(auditLog.getSubjectStudentName()).thenReturn(response.subjectStudentName());
        when(auditLog.getDormitoryId()).thenReturn(response.dormitoryId());
        when(auditLog.getDormitoryName()).thenReturn(response.dormitoryName());
        when(auditLog.getCategory()).thenReturn(response.category());
        when(auditLog.getAction()).thenReturn(response.action());
        when(auditLog.getEntityType()).thenReturn(response.entityType());
        when(auditLog.getEntityId()).thenReturn(response.entityId());
        when(auditLog.getTargetLabel()).thenReturn(response.targetLabel());
        when(auditLog.getDescription()).thenReturn(response.description());
        when(auditLog.getCreatedAt()).thenReturn(response.createdAt());
        return auditLog;
    }

    private AuditLogResponse response(Long id) {
        return new AuditLogResponse(id, 11L, "Student One", Role.STUDENT,
                12L, "Student One", 10L, "Dormitory", AuditCategory.STUDENT_ACTIVITY,
                AuditAction.DOCUMENT_UPLOADED, AuditEntityType.STUDENT_DOCUMENT,
                13L, "Identity Document", "uploaded", LocalDateTime.of(2026, 8, 12, 10, 0));
    }
}
