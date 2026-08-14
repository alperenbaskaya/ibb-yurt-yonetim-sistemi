package com.ibb.yurtlar.kafka;

import com.ibb.yurtlar.entity.OutboxEvent;
import com.ibb.yurtlar.enums.OutboxEventStatus;
import com.ibb.yurtlar.repository.OutboxEventRepository;
import com.ibb.yurtlar.service.OutboxEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class OutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(
                    OutboxPublisher.class
            );

    private static final int BATCH_SIZE = 20;

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventService outboxEventService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            OutboxEventService outboxEventService,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.outboxEventRepository =
                outboxEventRepository;

        this.outboxEventService =
                outboxEventService;

        this.kafkaTemplate =
                kafkaTemplate;
    }

    @Scheduled(
            fixedDelayString =
                    "${outbox.publisher.fixed-delay-ms:2000}",
            initialDelayString =
                    "${outbox.publisher.initial-delay-ms:3000}"
    )
    public void publishPendingEvents() {

        List<OutboxEvent> pendingEvents =
                outboxEventRepository
                        .findByStatusOldestFirst(
                                OutboxEventStatus.PENDING,
                                PageRequest.of(
                                        0,
                                        BATCH_SIZE
                                )
                        );

        for (OutboxEvent event : pendingEvents) {

            boolean published =
                    publish(event);

            if (!published) {
                break;
            }
        }
    }

    private boolean publish(
            OutboxEvent event
    ) {

        try {

            kafkaTemplate
                    .send(
                            event.getTopic(),
                            event.getEventKey(),
                            event.getPayload()
                    )
                    .get(5, TimeUnit.SECONDS);

            outboxEventService
                    .markPublished(
                            event.getId()
                    );

            log.info(
                    "Outbox event Kafka'ya gönderildi."
                            + " outboxId={}"
                            + " eventId={}"
                            + " type={}",
                    event.getId(),
                    event.getEventId(),
                    event.getEventType()
            );

            return true;

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            log.warn(
                    "Outbox publisher thread kesildi."
                            + " outboxId={}",
                    event.getId(),
                    exception
            );

            return false;

        } catch (
                ExecutionException
                | TimeoutException exception
        ) {

            log.warn(
                    "Outbox event Kafka'ya gönderilemedi."
                            + " PENDING olarak kalacak."
                            + " outboxId={}"
                            + " eventId={}",
                    event.getId(),
                    event.getEventId(),
                    exception
            );

            return false;
        }
    }
}