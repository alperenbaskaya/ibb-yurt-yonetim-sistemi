package com.ibb.yurtlar.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaLearningListener {

    @KafkaListener(
            topics = "dormitory-activity-events", //Topic: dormitory-activity-events
            groupId = "yurtlar-learning-group" //Consumer Group: yurtlar-learning-group
    )
    public void listen(ConsumerRecord<String, String> record) {

        System.out.println(
                "KAFKA MESSAGE RECEIVED"
                        + " | key=" + record.key()
                        + " | value=" + record.value()
                        + " | partition=" + record.partition()
                        + " | offset=" + record.offset()
        );
    }
}