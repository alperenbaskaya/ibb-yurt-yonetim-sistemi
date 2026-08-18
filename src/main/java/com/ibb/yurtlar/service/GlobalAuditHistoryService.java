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
import com.ibb.yurtlar.repository.DormitoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GlobalAuditHistoryService {
    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository appUserRepository;
    private final DormitoryRepository dormitoryRepository;

    public GlobalAuditHistoryService(
            AuditLogRepository auditLogRepository,
            AppUserRepository appUserRepository,
            DormitoryRepository dormitoryRepository
    ) {
        this.auditLogRepository = auditLogRepository;
        this.appUserRepository = appUserRepository;
        this.dormitoryRepository = dormitoryRepository;
    }

    @Transactional(readOnly = true)
    public AuditLogPageResponse getDormitoryOperations(
            Long dormitoryId,
            AuditCategory category,
            int page,
            int size,
            String authenticatedEmail
    ) {
        validateGlobalAdmin(authenticatedEmail);
        validatePage(page, size);
        if (category != AuditCategory.STUDENT_ACTIVITY
                && category != AuditCategory.REVIEWER_ACTIVITY) {
            throw new BusinessException(INVALID_AUDIT_HISTORY_REQUEST,
                    "Yurt operasyonları için yalnızca öğrenci veya değerlendirici işlem kategorisi kullanılabilir."
            );
        }
        if (!dormitoryRepository.existsById(dormitoryId)) {
            throw new BusinessException(DORMITORY_NOT_FOUND, dormitoryId);
        }

        return toPageResponse(auditLogRepository
                .findByDormitoryIdAndCategoryOrderByCreatedAtDescIdDesc(
                        dormitoryId,
                        category,
                        PageRequest.of(page, size)
                ));
    }

    @Transactional(readOnly = true)
    public AuditLogPageResponse getSystemManagement(
            int page,
            int size,
            String authenticatedEmail
    ) {
        validateGlobalAdmin(authenticatedEmail);
        validatePage(page, size);
        return toPageResponse(auditLogRepository
                .findByCategoryOrderByCreatedAtDescIdDesc(
                        AuditCategory.SYSTEM_MANAGEMENT,
                        PageRequest.of(page, size)
                ));
    }

    private void validateGlobalAdmin(String email) {
        AppUser user = appUserRepository.findByNormalizedEmail(email)
                .orElseThrow(() -> new BusinessException(INVALID_CREDENTIALS));
        if (!user.isActive()
                || user.getRole() != Role.ADMIN
                || user.getAdminScope() != AdminScope.GLOBAL) {
            throw new BusinessException(USER_MANAGEMENT_ACCESS_DENIED,
                    "Sistem geçmişini yalnızca aktif GLOBAL adminler görüntüleyebilir."
            );
        }
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(INVALID_AUDIT_HISTORY_REQUEST,
                    "Sayfa 0 veya daha büyük, sayfa boyutu 1 ile 100 arasında olmalıdır."
            );
        }
    }

    private AuditLogPageResponse toPageResponse(Page<AuditLog> auditPage) {
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
