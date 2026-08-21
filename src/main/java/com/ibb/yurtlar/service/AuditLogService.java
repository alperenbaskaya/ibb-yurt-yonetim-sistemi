package com.ibb.yurtlar.service;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.*;

import com.ibb.yurtlar.exception.BusinessException;

import com.ibb.yurtlar.entity.*;
import com.ibb.yurtlar.enums.*;
import com.ibb.yurtlar.event.AuditLogRecordedEvent;
import com.ibb.yurtlar.kafka.event.AuditLogRecordedKafkaEvent;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.AuditLogRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository appUserRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OutboxEventService outboxEventService;

    public AuditLogService(AuditLogRepository auditLogRepository,
                           AppUserRepository appUserRepository,
                           ApplicationEventPublisher eventPublisher,
                           OutboxEventService outboxEventService) {
        this.auditLogRepository = auditLogRepository;
        this.appUserRepository = appUserRepository;
        this.eventPublisher = eventPublisher;
        this.outboxEventService = outboxEventService;
    }

    @Transactional
    public void recordStudentEvent(String actorEmail, AuditCategory category,
                                   AuditAction action, AuditEntityType entityType,
                                   Long entityId, String targetLabel,
                                   Student student, Dormitory dormitory,
                                   String description) {
        record(actorEmail, category, action, entityType, entityId, targetLabel,
                student, dormitory, description);
    }

    @Transactional
    public void recordSystemEvent(String actorEmail, AuditAction action,
                                  AuditEntityType entityType, Long entityId,
                                  String targetLabel, Dormitory dormitory,
                                  String description) {
        record(actorEmail, AuditCategory.SYSTEM_MANAGEMENT, action, entityType,
                entityId, targetLabel, null, dormitory, description);
    }

    @Transactional
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
                .orElseThrow(() -> new BusinessException(INVALID_CREDENTIALS));
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

        String eventId = UUID.randomUUID().toString();
        AuditLogRecordedKafkaEvent kafkaEvent = new AuditLogRecordedKafkaEvent(
                eventId, "AUDIT_LOG_RECORDED", savedAuditLog.getId(),
                savedAuditLog.getActorUserId(), savedAuditLog.getActorName(),
                savedAuditLog.getActorRole(), savedAuditLog.getSubjectStudentId(),
                savedAuditLog.getSubjectStudentName(), savedAuditLog.getDormitoryId(),
                savedAuditLog.getDormitoryName(), savedAuditLog.getCategory(),
                savedAuditLog.getAction(), savedAuditLog.getEntityType(),
                savedAuditLog.getEntityId(), savedAuditLog.getTargetLabel(),
                savedAuditLog.getDescription(), savedAuditLog.getCreatedAt(), 1
        );
        outboxEventService.recordPending(
                eventId,
                "AUDIT_LOG_RECORDED",
                "AUDIT_LOG",
                savedAuditLog.getId(),
                "yurtlar-audit-log-events",
                "audit-log-" + savedAuditLog.getId(),
                kafkaEvent
        );

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
