package com.ibb.yurtlar.config;

import com.ibb.yurtlar.exception.InvalidKafkaEventPayloadException;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import com.ibb.yurtlar.observability.KafkaMetricsService;

@Configuration
public class AuditLogProjectionKafkaConfig {

    private static final Logger log =
            LoggerFactory.getLogger(AuditLogProjectionKafkaConfig.class);
    private static final String DLT_TOPIC =
            "yurtlar-audit-log-events.DLT";

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    auditLogProjectionKafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            KafkaTemplate<String, String> kafkaTemplate,
            KafkaMetricsService metrics
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(
                ContainerProperties.AckMode.RECORD
        );

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) ->
                                new TopicPartition(DLT_TOPIC, record.partition())
                );
        recoverer.setFailIfSendResultIsError(true);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, exception) -> {
                    recoverer.accept(record, exception);
                    metrics.deadLetter("AUDIT_LOG_RECORDED",
                            "audit-log-elasticsearch-projection-v1");
                },
                new FixedBackOff(1000L, 3L)
        );
        errorHandler.addNotRetryableExceptions(
                InvalidKafkaEventPayloadException.class
        );
        errorHandler.setRetryListeners(
                (record, exception, deliveryAttempt) -> {
                        metrics.processingError("AUDIT_LOG_RECORDED",
                                "audit-log-elasticsearch-projection-v1");
                        log.warn(
                                "Audit projection delivery failed."
                                        + " topic={} partition={} offset={}"
                                        + " attempt={} exception={}",
                                record.topic(),
                                record.partition(),
                                record.offset(),
                                deliveryAttempt,
                                exception.getClass().getSimpleName()
                        );
                }
        );
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}
