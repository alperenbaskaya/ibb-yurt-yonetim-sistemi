package com.ibb.yurtlar.search.audit;

import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.AuditEntityType;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.exception.InvalidKafkaEventPayloadException;
import com.ibb.yurtlar.kafka.event.AuditLogRecordedKafkaEvent;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import com.ibb.yurtlar.observability.ElasticsearchMetricsService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditLogIndexConsumerTest {

    private final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private final AuditLogIndexMapper mapper = new AuditLogIndexMapper();
    private final AuditLogSearchRepository repository =
            mock(AuditLogSearchRepository.class);
    private final ElasticsearchMetricsService metrics = mock(ElasticsearchMetricsService.class);
    private final AuditLogIndexConsumer consumer =
            new AuditLogIndexConsumer(objectMapper, mapper, repository, metrics,
                    "yurtlar-audit-log-v1");

    @Test
    void validEventIsIndexed() {
        AuditLogRecordedKafkaEvent event = validEvent(100L);

        consumer.process(event);

        verify(repository).save(any(AuditLogSearchDocument.class));
        verify(metrics).indexSuccess();
    }

    @Test
    void sameAuditLogIdOverwritesOneLogicalDocument() {
        Map<String, AuditLogSearchDocument> indexedDocuments = new HashMap<>();
        doAnswer(invocation -> {
            AuditLogSearchDocument document = invocation.getArgument(0);
            indexedDocuments.put(document.getId(), document);
            return document;
        }).when(repository).save(any(AuditLogSearchDocument.class));

        consumer.process(validEvent(200L));
        consumer.process(validEvent(200L));

        assertThat(indexedDocuments).hasSize(1).containsKey("200");
        verify(repository, org.mockito.Mockito.times(2))
                .save(any(AuditLogSearchDocument.class));
    }

    @Test
    void invalidEventTypeIsRejected() {
        AuditLogRecordedKafkaEvent event = withProtocol(
                validEvent(300L), "OTHER_EVENT", 1);

        assertThatThrownBy(() -> consumer.process(event))
                .isInstanceOf(InvalidKafkaEventPayloadException.class)
                .hasMessageContaining("eventType");
        verify(repository, never()).save(any());
    }

    @Test
    void unsupportedSchemaVersionIsRejected() {
        AuditLogRecordedKafkaEvent event = withProtocol(
                validEvent(400L), "AUDIT_LOG_RECORDED", 2);

        assertThatThrownBy(() -> consumer.process(event))
                .isInstanceOf(InvalidKafkaEventPayloadException.class)
                .hasMessageContaining("schemaVersion");
        verify(repository, never()).save(any());
    }

    @Test
    void indexingFailurePropagatesWithoutDatabaseSideEffects() {
        RuntimeException failure = new RuntimeException("Elasticsearch unavailable");
        when(repository.save(any(AuditLogSearchDocument.class)))
                .thenThrow(failure);

        assertThatThrownBy(() -> consumer.process(validEvent(500L)))
                .isSameAs(failure);

        verify(repository).save(any(AuditLogSearchDocument.class));
        verify(metrics).indexFailure();
    }

    private AuditLogRecordedKafkaEvent validEvent(Long auditLogId) {
        return new AuditLogRecordedKafkaEvent(
                "event-" + auditLogId,
                "AUDIT_LOG_RECORDED",
                auditLogId,
                11L,
                "Actor Name",
                Role.ADMIN,
                22L,
                "Student Name",
                33L,
                "Dormitory Name",
                AuditCategory.SYSTEM_MANAGEMENT,
                AuditAction.USER_UPDATED,
                AuditEntityType.USER,
                44L,
                "Target User",
                "User was updated.",
                LocalDateTime.of(2026, 8, 18, 15, 0),
                1
        );
    }

    private AuditLogRecordedKafkaEvent withProtocol(
            AuditLogRecordedKafkaEvent event,
            String eventType,
            int schemaVersion
    ) {
        return new AuditLogRecordedKafkaEvent(
                event.eventId(), eventType, event.auditLogId(),
                event.actorUserId(), event.actorName(), event.actorRole(),
                event.subjectStudentId(), event.subjectStudentName(),
                event.dormitoryId(), event.dormitoryName(), event.category(),
                event.action(), event.entityType(), event.entityId(),
                event.targetLabel(), event.description(), event.createdAt(),
                schemaVersion
        );
    }
}
