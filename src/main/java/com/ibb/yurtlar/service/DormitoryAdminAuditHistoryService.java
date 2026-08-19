package com.ibb.yurtlar.service;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.*;

import com.ibb.yurtlar.exception.BusinessException;

import com.ibb.yurtlar.dto.AuditLogPageResponse;
import com.ibb.yurtlar.dto.AuditLogResponse;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.AuditLog;
import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.AuditLogRepository;
import com.ibb.yurtlar.search.audit.AuditLogSearchCriteria;
import com.ibb.yurtlar.search.audit.AuditLogSearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DormitoryAdminAuditHistoryService {
    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository appUserRepository;
    private final AuditLogSearchService auditLogSearchService;

    public DormitoryAdminAuditHistoryService(
            AuditLogRepository auditLogRepository,
            AppUserRepository appUserRepository,
            AuditLogSearchService auditLogSearchService
    ) {
        this.auditLogRepository = auditLogRepository;
        this.appUserRepository = appUserRepository;
        this.auditLogSearchService = auditLogSearchService;
    }

    @Transactional(readOnly = true)
    public AuditLogPageResponse searchOwnDormitory(
            AuditLogSearchCriteria criteria, String authenticatedEmail) {
        AppUser admin = validateDormitoryAdmin(authenticatedEmail);
        return auditLogSearchService.search(criteria, admin.getDormitory().getId());
    }

    @Transactional(readOnly = true)
    public AuditLogPageResponse getOwnDormitoryHistory(
            AuditCategory category,
            int page,
            int size,
            String authenticatedEmail
    ) {
        AppUser admin = validateDormitoryAdmin(authenticatedEmail);
        validateRequest(category, page, size);

        Page<AuditLog> auditPage = auditLogRepository
                .findByDormitoryIdAndCategoryOrderByCreatedAtDescIdDesc(
                        admin.getDormitory().getId(),
                        category,
                        PageRequest.of(page, size)
                );

        return new AuditLogPageResponse(
                auditPage.getContent().stream().map(this::toResponse).toList(),
                auditPage.getNumber(),
                auditPage.getSize(),
                auditPage.getTotalElements(),
                auditPage.getTotalPages(),
                auditPage.isFirst(),
                auditPage.isLast()
        );
    }

    private AppUser validateDormitoryAdmin(String email) {
        AppUser user = appUserRepository.findByNormalizedEmail(email)
                .orElseThrow(() -> new BusinessException(INVALID_CREDENTIALS));
        if (!user.isActive()
                || user.getRole() != Role.ADMIN
                || user.getAdminScope() != AdminScope.DORMITORY
                || user.getDormitory() == null) {
            throw new BusinessException(USER_MANAGEMENT_ACCESS_DENIED,
                    "Yurt işlem geçmişini yalnızca yurt ataması bulunan aktif yurt adminleri görüntüleyebilir."
            );
        }
        return user;
    }

    private void validateRequest(AuditCategory category, int page, int size) {
        if (category != AuditCategory.STUDENT_ACTIVITY
                && category != AuditCategory.REVIEWER_ACTIVITY) {
            throw new BusinessException(INVALID_AUDIT_HISTORY_REQUEST,
                    "Yurt işlem geçmişinde yalnızca öğrenci veya değerlendirici işlemleri görüntülenebilir."
            );
        }
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(INVALID_AUDIT_HISTORY_REQUEST,
                    "Sayfa 0 veya daha büyük, sayfa boyutu 1 ile 100 arasında olmalıdır."
            );
        }
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getActorUserId(),
                auditLog.getActorName(),
                auditLog.getActorRole(),
                auditLog.getSubjectStudentId(),
                auditLog.getSubjectStudentName(),
                auditLog.getDormitoryId(),
                auditLog.getDormitoryName(),
                auditLog.getCategory(),
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getTargetLabel(),
                auditLog.getDescription(),
                auditLog.getCreatedAt()
        );
    }
}
