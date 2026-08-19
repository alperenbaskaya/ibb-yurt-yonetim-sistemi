package com.ibb.yurtlar.search.audit;

import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.AuditEntityType;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.kafka.event.AuditLogRecordedKafkaEvent;
import com.ibb.yurtlar.entity.AuditLog;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.test.util.ReflectionTestUtils;

class AuditLogIndexMapperTest {

    private final AuditLogIndexMapper mapper = new AuditLogIndexMapper();

    @Test
    void mapsEveryEntityFieldForBackfill() {
        AuditLog auditLog = new AuditLog(11L, "Actor Name", Role.REVIEWER,
                22L, "Student Name", 33L, "Dormitory Name",
                AuditCategory.STUDENT_ACTIVITY, AuditAction.DOCUMENT_APPROVED,
                AuditEntityType.STUDENT_DOCUMENT, 44L, "Identity Document",
                "Document was approved.");
        ReflectionTestUtils.setField(auditLog, "id", 101L);
        ReflectionTestUtils.setField(auditLog, "createdAt",
                LocalDateTime.of(2026, 8, 18, 14, 30));

        AuditLogSearchDocument document = mapper.toDocument(auditLog);

        assertThat(document.getId()).isEqualTo("101");
        assertThat(document.getAuditLogId()).isEqualTo(101L);
        assertThat(document.getActorName()).isEqualTo("Actor Name");
        assertThat(document.getDormitoryId()).isEqualTo(33L);
        assertThat(document.getDescription()).isEqualTo("Document was approved.");
        assertThat(document.getSchemaVersion()).isEqualTo(1);
    }

    @Test
    void mapsEveryEventFieldAndUsesAuditLogIdAsDocumentId() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 18, 14, 30);
        AuditLogRecordedKafkaEvent event = new AuditLogRecordedKafkaEvent(
                "event-123",
                "AUDIT_LOG_RECORDED",
                101L,
                11L,
                "Actor Name",
                Role.REVIEWER,
                22L,
                "Student Name",
                33L,
                "Dormitory Name",
                AuditCategory.STUDENT_ACTIVITY,
                AuditAction.DOCUMENT_APPROVED,
                AuditEntityType.STUDENT_DOCUMENT,
                44L,
                "Identity Document",
                "Document was approved.",
                createdAt,
                1
        );

        AuditLogSearchDocument document = mapper.toDocument(event);

        assertThat(document.getId()).isEqualTo("101");
        assertThat(document.getAuditLogId()).isEqualTo(101L);
        assertThat(document.getActorUserId()).isEqualTo(11L);
        assertThat(document.getActorName()).isEqualTo("Actor Name");
        assertThat(document.getActorRole()).isEqualTo(Role.REVIEWER);
        assertThat(document.getSubjectStudentId()).isEqualTo(22L);
        assertThat(document.getSubjectStudentName()).isEqualTo("Student Name");
        assertThat(document.getDormitoryId()).isEqualTo(33L);
        assertThat(document.getDormitoryName()).isEqualTo("Dormitory Name");
        assertThat(document.getCategory()).isEqualTo(AuditCategory.STUDENT_ACTIVITY);
        assertThat(document.getAction()).isEqualTo(AuditAction.DOCUMENT_APPROVED);
        assertThat(document.getEntityType()).isEqualTo(AuditEntityType.STUDENT_DOCUMENT);
        assertThat(document.getEntityId()).isEqualTo(44L);
        assertThat(document.getTargetLabel()).isEqualTo("Identity Document");
        assertThat(document.getDescription()).isEqualTo("Document was approved.");
        assertThat(document.getCreatedAt()).isEqualTo(createdAt);
        assertThat(document.getSchemaVersion()).isEqualTo(1);
    }

    @Test
    void converterKeepsAuditLogIdButOmitsMetadataFieldsFromSource() throws Exception {
        SimpleElasticsearchMappingContext mappingContext =
                new SimpleElasticsearchMappingContext();
        mappingContext.setInitialEntitySet(Set.of(AuditLogSearchDocument.class));
        mappingContext.afterPropertiesSet();

        MappingElasticsearchConverter converter =
                new MappingElasticsearchConverter(mappingContext);
        converter.afterPropertiesSet();

        Document target = Document.create();
        converter.write(mapper.toDocument(event(101L)), target);

        assertThat(target).containsEntry("auditLogId", 101L);
        assertThat(target).doesNotContainKeys("_class", "id");
    }

    private AuditLogRecordedKafkaEvent event(Long auditLogId) {
        return new AuditLogRecordedKafkaEvent(
                "event-" + auditLogId,
                "AUDIT_LOG_RECORDED",
                auditLogId,
                11L,
                "Actor Name",
                Role.REVIEWER,
                22L,
                "Student Name",
                33L,
                "Dormitory Name",
                AuditCategory.STUDENT_ACTIVITY,
                AuditAction.DOCUMENT_APPROVED,
                AuditEntityType.STUDENT_DOCUMENT,
                44L,
                "Identity Document",
                "Document was approved.",
                LocalDateTime.of(2026, 8, 18, 14, 30),
                1
        );
    }
}
