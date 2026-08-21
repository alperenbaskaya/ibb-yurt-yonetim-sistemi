package com.ibb.yurtlar.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaLearningProducer {

    private static final String TOPIC = "dormitory-activity-events";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaLearningProducer(
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(
            String key,
            String message
    ) {
        kafkaTemplate.send(TOPIC, key, message);
    }
}