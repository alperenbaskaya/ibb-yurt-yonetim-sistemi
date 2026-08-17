package com.ibb.yurtlar.config;

import com.ibb.yurtlar.exception.InvalidKafkaEventPayloadException;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class KafkaErrorHandlingConfig {

    private static final Logger log =
            LoggerFactory.getLogger(KafkaErrorHandlingConfig.class);

    private static final String DLT_TOPIC =
            "dormitory-activity-events-dlt";

    @Bean
    public NewTopic dormitoryActivityEventsDltTopic() {

        return TopicBuilder
                .name(DLT_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    documentUploadedKafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            KafkaTemplate<String, String> kafkaTemplate
    ) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                consumerFactory
        );

        factory.getContainerProperties().setAckMode(
                ContainerProperties.AckMode.RECORD
        );

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) ->
                                new TopicPartition(
                                        DLT_TOPIC,
                                        -1
                                )
                );

        recoverer.setFailIfSendResultIsError(true);

        FixedBackOff fixedBackOff =
                new FixedBackOff(
                        1000L,
                        2L
                );

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(
                        recoverer,
                        fixedBackOff
                );

        errorHandler.setRetryListeners(
                (record, exception, deliveryAttempt) ->
                        log.warn(
                                "Kafka delivery başarısız."
                                        + " topic={}"
                                        + " partition={}"
                                        + " offset={}"
                                        + " attempt={}"
                                        + " exception={}",
                                record.topic(),
                                record.partition(),
                                record.offset(),
                                deliveryAttempt,
                                exception.getClass().getSimpleName()
                        )
        );

        errorHandler.addNotRetryableExceptions(
                InvalidKafkaEventPayloadException.class
        );

        factory.setCommonErrorHandler(
                errorHandler
        );

        return factory;
    }
}