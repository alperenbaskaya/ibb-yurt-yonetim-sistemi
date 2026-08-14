package com.ibb.yurtlar.service;

import com.ibb.yurtlar.entity.OutboxEvent;
import com.ibb.yurtlar.enums.OutboxEventStatus;
import com.ibb.yurtlar.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxEventService(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper
    ) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public OutboxEvent recordPending(
            String eventId,
            String eventType,
            String aggregateType,
            Long aggregateId,
            String topic,
            String eventKey,
            Object payload
    ) {

        OutboxEvent event = new OutboxEvent();

        event.setEventId(eventId);
        event.setEventType(eventType);
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);

        event.setTopic(topic);
        event.setEventKey(eventKey);

        event.setPayload(
                serializePayload(payload)
        );

        event.setStatus(
                OutboxEventStatus.PENDING
        );

        event.setCreatedAt(
                LocalDateTime.now()
        );

        return outboxEventRepository.save(event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPublished(Long outboxEventId) {

        OutboxEvent event =
                outboxEventRepository
                        .findById(outboxEventId)
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "Outbox event bulunamadı. ID: "
                                                + outboxEventId
                                )
                        );

        if (event.getStatus()
                == OutboxEventStatus.PUBLISHED) {
            return;
        }

        event.setStatus(
                OutboxEventStatus.PUBLISHED
        );

        event.setPublishedAt(
                LocalDateTime.now()
        );
    }

    private String serializePayload(Object payload) {

        try {
            return objectMapper.writeValueAsString(payload);

        } catch (JacksonException exception) {

            throw new IllegalStateException(
                    "Outbox event payload JSON'a dönüştürülemedi.",
                    exception
            );
        }
    }
}