package com.ibb.yurtlar.search.audit;

import com.ibb.yurtlar.exception.InvalidKafkaEventPayloadException;
import com.ibb.yurtlar.kafka.event.AuditLogRecordedKafkaEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.ibb.yurtlar.observability.ElasticsearchMetricsService;

@Component
@Profile("!test")
public class AuditLogIndexConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(AuditLogIndexConsumer.class);

    private final ObjectMapper objectMapper;
    private final AuditLogIndexMapper mapper;
    private final AuditLogSearchRepository repository;
    private final ElasticsearchMetricsService metrics;

    public AuditLogIndexConsumer(
            ObjectMapper objectMapper,
            AuditLogIndexMapper mapper,
            AuditLogSearchRepository repository,
            ElasticsearchMetricsService metrics
    ) {
        this.objectMapper = objectMapper;
        this.mapper = mapper;
        this.repository = repository;
        this.metrics = metrics;
    }

    @KafkaListener(
            topics = "yurtlar-audit-log-events",
            groupId = "audit-log-elasticsearch-projection-v1",
            containerFactory = "auditLogProjectionKafkaListenerContainerFactory"
    )
    public void listen(ConsumerRecord<String, String> record) {
        AuditLogRecordedKafkaEvent event = deserialize(record.value());
        process(event);

        log.info(
                "Audit log Elasticsearch projection indexed."
                        + " auditLogId={} partition={} offset={}",
                event.auditLogId(),
                record.partition(),
                record.offset()
        );
    }

    public void process(AuditLogRecordedKafkaEvent event) {
        validate(event);
        try {
            repository.save(mapper.toDocument(event));
            metrics.indexSuccess();
        } catch (RuntimeException exception) {
            metrics.indexFailure();
            throw exception;
        }
    }

    private AuditLogRecordedKafkaEvent deserialize(String payload) {
        try {
            return objectMapper.readValue(
                    payload,
                    AuditLogRecordedKafkaEvent.class
            );
        } catch (JacksonException exception) {
            throw new InvalidKafkaEventPayloadException(
                    "AUDIT_LOG_RECORDED Kafka payload could not be read.",
                    exception
            );
        }
    }

    private void validate(AuditLogRecordedKafkaEvent event) {
        if (event == null) {
            throw invalid("event is null");
        }
        if (!"AUDIT_LOG_RECORDED".equals(event.eventType())) {
            throw invalid("eventType must be AUDIT_LOG_RECORDED");
        }
        if (event.eventId() == null || event.eventId().isBlank()) {
            throw invalid("eventId is blank");
        }
        if (event.auditLogId() == null) {
            throw invalid("auditLogId is null");
        }
        if (event.schemaVersion() != 1) {
            throw invalid("schemaVersion is unsupported: " + event.schemaVersion());
        }
        if (event.actorUserId() == null
                || event.actorName() == null || event.actorName().isBlank()
                || event.actorRole() == null
                || event.category() == null
                || event.action() == null
                || event.entityType() == null
                || event.entityId() == null
                || event.targetLabel() == null || event.targetLabel().isBlank()
                || event.description() == null || event.description().isBlank()
                || event.createdAt() == null) {
            throw invalid("required projection field is missing");
        }
    }

    private InvalidKafkaEventPayloadException invalid(String detail) {
        return new InvalidKafkaEventPayloadException(
                "Invalid AUDIT_LOG_RECORDED event: " + detail
        );
    }
}
