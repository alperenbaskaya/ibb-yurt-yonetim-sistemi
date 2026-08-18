package com.ibb.yurtlar.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class AuditLogKafkaTopicConfig {

    @Bean
    public NewTopic auditLogEventsTopic() {
        return TopicBuilder
                .name("yurtlar-audit-log-events")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic auditLogEventsDltTopic() {
        return TopicBuilder
                .name("yurtlar-audit-log-events.DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
