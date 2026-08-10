package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.AuditLogResponse;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.AuditLog;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.exception.InvalidCredentialsException;
import com.ibb.yurtlar.exception.InvalidUserConfigurationException;
import com.ibb.yurtlar.exception.UserIsNotReviewerException;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.AuditLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewerActivityService {
    private static final int RECENT_ACTIVITY_LIMIT = 5;
    private static final List<AuditAction> INCLUDED_ACTIONS = List.of(
            AuditAction.DOCUMENT_UPLOADED,
            AuditAction.DOCUMENT_REUPLOADED
    );

    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository appUserRepository;

    public ReviewerActivityService(
            AuditLogRepository auditLogRepository,
            AppUserRepository appUserRepository
    ) {
        this.auditLogRepository = auditLogRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getRecentActivity(String authenticatedEmail) {
        AppUser reviewer = appUserRepository.findByNormalizedEmail(authenticatedEmail)
                .orElseThrow(InvalidCredentialsException::new);
        if (reviewer.getRole() != Role.REVIEWER) {
            throw new UserIsNotReviewerException(reviewer.getId());
        }
        if (!reviewer.isActive()) {
            throw new InvalidCredentialsException();
        }

        Dormitory dormitory = reviewer.getDormitory();
        if (dormitory == null) {
            throw new InvalidUserConfigurationException(
                    "Değerlendirici kullanıcısına bir yurt atanmamıştır."
            );
        }

        return auditLogRepository.findRecentByDormitoryCategoryAndActions(
                        dormitory.getId(),
                        AuditCategory.STUDENT_ACTIVITY,
                        INCLUDED_ACTIONS,
                        PageRequest.of(0, RECENT_ACTIVITY_LIMIT)
                ).stream()
                .map(this::toResponse)
                .toList();
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
