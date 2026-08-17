package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.ProcessedKafkaEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedKafkaEventRepository
        extends JpaRepository<ProcessedKafkaEvent, Long> {

    boolean existsByConsumerNameAndEventId(
            String consumerName,
            String eventId
    );
}