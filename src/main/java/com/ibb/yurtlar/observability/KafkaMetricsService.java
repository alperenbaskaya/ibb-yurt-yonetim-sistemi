package com.ibb.yurtlar.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class KafkaMetricsService {
    private static final Logger log = LoggerFactory.getLogger(KafkaMetricsService.class);
    private static final Set<String> EVENT_TYPES = Set.of("DOCUMENT_UPLOADED", "AUDIT_LOG_RECORDED");
    private static final Set<String> CONSUMERS = Set.of(
            "document-upload-idempotency-group-v1", "audit-log-elasticsearch-projection-v1");

    private final MeterRegistry registry;

    public KafkaMetricsService(MeterRegistry registry) {
        this.registry = registry;
    }

    public void processingError(String eventType, String consumer) {
        increment("yurtlar_kafka_processing_errors_total", eventType, consumer);
    }

    public void deadLetter(String eventType, String consumer) {
        increment("yurtlar_kafka_dlt_total", eventType, consumer);
    }

    public void duplicate(String eventType, String consumer) {
        increment("yurtlar_kafka_duplicates_total", eventType, consumer);
    }

    private void increment(String name, String eventType, String consumer) {
        try {
            registry.counter(name,
                    "eventType", EVENT_TYPES.contains(eventType) ? eventType : "UNKNOWN",
                    "consumer", CONSUMERS.contains(consumer) ? consumer : "UNKNOWN").increment();
        } catch (RuntimeException metricsFailure) {
            log.debug("Kafka metric recording failed", metricsFailure);
        }
    }
}
