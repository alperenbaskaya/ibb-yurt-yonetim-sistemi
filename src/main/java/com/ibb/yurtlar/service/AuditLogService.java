package com.ibb.yurtlar.service;

import com.ibb.yurtlar.entity.*;
import com.ibb.yurtlar.enums.*;
import com.ibb.yurtlar.exception.InvalidCredentialsException;
import com.ibb.yurtlar.event.AuditLogRecordedEvent;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.AuditLogRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository appUserRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AuditLogService(AuditLogRepository auditLogRepository,
                           AppUserRepository appUserRepository,
                           ApplicationEventPublisher eventPublisher) {
        this.auditLogRepository = auditLogRepository;
        this.appUserRepository = appUserRepository;
        this.eventPublisher = eventPublisher;
    }

    public void recordStudentEvent(String actorEmail, AuditCategory category,
                                   AuditAction action, AuditEntityType entityType,
                                   Long entityId, String targetLabel,
                                   Student student, Dormitory dormitory,
                                   String description) {
        record(actorEmail, category, action, entityType, entityId, targetLabel,
                student, dormitory, description);
    }

    public void recordSystemEvent(String actorEmail, AuditAction action,
                                  AuditEntityType entityType, Long entityId,
                                  String targetLabel, Dormitory dormitory,
                                  String description) {
        record(actorEmail, AuditCategory.SYSTEM_MANAGEMENT, action, entityType,
                entityId, targetLabel, null, dormitory, description);
    }

    public void recordDocumentProcessCompletedIfAbsent(
            String actorEmail, Admission admission, String description) {
        if (auditLogRepository.existsByActionAndEntityTypeAndEntityId(
                AuditAction.DOCUMENT_PROCESS_COMPLETED,
                AuditEntityType.ADMISSION,
                admission.getId())) {
            return;
        }
        record(actorEmail, AuditCategory.STUDENT_ACTIVITY,
                AuditAction.DOCUMENT_PROCESS_COMPLETED, AuditEntityType.ADMISSION,
                admission.getId(), admission.getStudent().getUser().getFirstName()
                        + " " + admission.getStudent().getUser().getLastName(),
                admission.getStudent(), admission.getDormitory(), description);
    }

    private void record(String actorEmail, AuditCategory category,
                        AuditAction action, AuditEntityType entityType,
                        Long entityId, String targetLabel, Student student,
                        Dormitory dormitory, String description) {
        AppUser actor = appUserRepository.findByNormalizedEmail(actorEmail)
                .orElseThrow(InvalidCredentialsException::new);
        String actorName = actor.getFirstName() + " " + actor.getLastName();
        String studentName = student == null ? null
                : student.getUser().getFirstName() + " " + student.getUser().getLastName();

        AuditLog savedAuditLog = auditLogRepository.save(new AuditLog(
                actor.getId(), actorName, actor.getRole(),
                student == null ? null : student.getId(), studentName,
                dormitory == null ? null : dormitory.getId(),
                dormitory == null ? null : dormitory.getName(),
                category, action, entityType, entityId,
                targetLabel, description
        ));
        eventPublisher.publishEvent(new AuditLogRecordedEvent(
                savedAuditLog.getId(), savedAuditLog.getActorUserId(),
                savedAuditLog.getActorName(), savedAuditLog.getActorRole(),
                savedAuditLog.getSubjectStudentId(), savedAuditLog.getSubjectStudentName(),
                savedAuditLog.getDormitoryId(), savedAuditLog.getDormitoryName(),
                savedAuditLog.getCategory(), savedAuditLog.getAction(),
                savedAuditLog.getEntityType(), savedAuditLog.getEntityId(),
                savedAuditLog.getTargetLabel(), savedAuditLog.getDescription(),
                savedAuditLog.getCreatedAt()
        ));
    }
}
