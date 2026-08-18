package com.ibb.yurtlar.service;

import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.AuditLog;
import com.ibb.yurtlar.entity.OutboxEvent;
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.AuditEntityType;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.kafka.event.AuditLogRecordedKafkaEvent;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.AuditLogRepository;
import com.ibb.yurtlar.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "outbox.publisher.initial-delay-ms=3600000")
@ActiveProfiles("test")
class AuditLogOutboxIntegrationTest {

    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private AuditLogRepository auditLogRepository;
    @Autowired
    private OutboxEventRepository outboxEventRepository;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TransactionTemplate transactionTemplate;

    private String actorEmail;

    @BeforeEach
    void createActor() {
        actorEmail = "audit-outbox-" + UUID.randomUUID() + "@example.com";
        AppUser actor = new AppUser();
        actor.setFirstName("Audit");
        actor.setLastName("Admin");
        actor.setEmail(actorEmail);
        actor.setPasswordHash("not-used-in-test");
        actor.setRole(Role.ADMIN);
        actor.setActive(true);
        appUserRepository.save(actor);
    }

    @Test
    void auditCreationRecordsMatchingOutboxEventAndKeepsExistingAuditData() throws Exception {
        long auditCountBefore = auditLogRepository.count();
        long outboxCountBefore = outboxEventRepository.count();

        auditLogService.recordSystemEvent(
                actorEmail,
                AuditAction.USER_CREATED,
                AuditEntityType.USER,
                42L,
                "Created User",
                null,
                "Created User kullanıcısı oluşturuldu."
        );

        assertThat(auditLogRepository.count()).isEqualTo(auditCountBefore + 1);
        assertThat(outboxEventRepository.count()).isEqualTo(outboxCountBefore + 1);

        AuditLog auditLog = auditLogRepository.findAll().stream()
                .max((left, right) -> Long.compare(left.getId(), right.getId()))
                .orElseThrow();
        OutboxEvent outboxEvent = outboxEventRepository.findAll().stream()
                .filter(event -> event.getAggregateId().equals(auditLog.getId()))
                .findFirst()
                .orElseThrow();
        AuditLogRecordedKafkaEvent payload = objectMapper.readValue(
                outboxEvent.getPayload(), AuditLogRecordedKafkaEvent.class);

        assertThat(auditLog.getActorName()).isEqualTo("Audit Admin");
        assertThat(auditLog.getCategory()).isEqualTo(AuditCategory.SYSTEM_MANAGEMENT);
        assertThat(auditLog.getAction()).isEqualTo(AuditAction.USER_CREATED);
        assertThat(auditLog.getEntityId()).isEqualTo(42L);
        assertThat(auditLog.getCreatedAt()).isNotNull();

        assertThat(outboxEvent.getEventType()).isEqualTo("AUDIT_LOG_RECORDED");
        assertThat(outboxEvent.getAggregateType()).isEqualTo("AUDIT_LOG");
        assertThat(outboxEvent.getAggregateId()).isEqualTo(auditLog.getId());
        assertThat(outboxEvent.getTopic()).isEqualTo("yurtlar-audit-log-events");
        assertThat(outboxEvent.getEventKey()).isEqualTo("audit-log-" + auditLog.getId());
        assertThat(payload.auditLogId()).isEqualTo(auditLog.getId());
        assertThat(payload.eventId()).isEqualTo(outboxEvent.getEventId());
        assertThat(payload.eventType()).isEqualTo("AUDIT_LOG_RECORDED");
        assertThat(payload.schemaVersion()).isEqualTo(1);
    }

    @Test
    void rollingBackCallerTransactionRemovesAuditAndOutboxTogether() {
        long auditCountBefore = auditLogRepository.count();
        long outboxCountBefore = outboxEventRepository.count();

        transactionTemplate.executeWithoutResult(status -> {
            auditLogService.recordSystemEvent(
                    actorEmail,
                    AuditAction.USER_UPDATED,
                    AuditEntityType.USER,
                    84L,
                    "Rolled Back User",
                    null,
                    "This audit and its outbox intent must roll back."
            );
            assertThat(auditLogRepository.count()).isEqualTo(auditCountBefore + 1);
            assertThat(outboxEventRepository.count()).isEqualTo(outboxCountBefore + 1);
            status.setRollbackOnly();
        });

        assertThat(auditLogRepository.count()).isEqualTo(auditCountBefore);
        assertThat(outboxEventRepository.count()).isEqualTo(outboxCountBefore);
    }
}
