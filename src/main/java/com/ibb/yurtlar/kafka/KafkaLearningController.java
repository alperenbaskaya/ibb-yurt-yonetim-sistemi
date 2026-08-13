package com.ibb.yurtlar.kafka;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kafka/learning")
public class KafkaLearningController {

    private final KafkaLearningProducer kafkaLearningProducer;

    public KafkaLearningController(
            KafkaLearningProducer kafkaLearningProducer
    ) {
        this.kafkaLearningProducer = kafkaLearningProducer;
    }

    @PostMapping("/send")
    public ResponseEntity<String> send(
            @RequestParam String key,
            @RequestParam String message
    ) {
        kafkaLearningProducer.send(key, message);

        return ResponseEntity.ok("Kafka mesajı gönderildi.");
    }
}