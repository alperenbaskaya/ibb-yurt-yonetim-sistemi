package com.ibb.yurtlar.kafka;

import com.ibb.yurtlar.exception.DuplicateKafkaEventException;
import com.ibb.yurtlar.kafka.event.DocumentUploadedEvent;
import com.ibb.yurtlar.service.DocumentUploadedEventConsumerService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.ibb.yurtlar.exception.InvalidKafkaEventPayloadException;

@Component
public class DocumentUploadedEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    DocumentUploadedEventListener.class
            );

    private final ObjectMapper objectMapper;

    private final DocumentUploadedEventConsumerService
            consumerService;

    public DocumentUploadedEventListener(
            ObjectMapper objectMapper,
            DocumentUploadedEventConsumerService consumerService
    ) {
        this.objectMapper = objectMapper;
        this.consumerService = consumerService;
    }

    @KafkaListener(
            topics = "dormitory-activity-events",
            groupId = "document-upload-idempotency-group-v1",
            containerFactory =
                    "documentUploadedKafkaListenerContainerFactory",
            properties = {
                    "auto.offset.reset=latest"
            }
    )
    public void listen(
            ConsumerRecord<String, String> record
    ) {

        DocumentUploadedEvent event =
                deserialize(record.value());

        if (!"DOCUMENT_UPLOADED".equals(
                event.eventType()
        )) {
            return;
        }

        try {

            consumerService.process(event);

            log.info(
                    "DOCUMENT_UPLOADED event işlendi."
                            + " eventId={}"
                            + " studentId={}"
                            + " documentId={}"
                            + " partition={}"
                            + " offset={}",
                    event.eventId(),
                    event.studentId(),
                    event.documentId(),
                    record.partition(),
                    record.offset()
            );

        } catch (DuplicateKafkaEventException exception) {

            log.info(
                    "Duplicate Kafka event atlandı."
                            + " eventId={}"
                            + " partition={}"
                            + " offset={}",
                    event.eventId(),
                    record.partition(),
                    record.offset()
            );
        }
    }

    private DocumentUploadedEvent deserialize(
            String payload
    ) {

        try {

            return objectMapper.readValue(
                    payload,
                    DocumentUploadedEvent.class
            );

        } catch (JacksonException exception) {

            throw new InvalidKafkaEventPayloadException(
                    "DOCUMENT_UPLOADED Kafka payload okunamadı.",
                    exception
            );
        }
    }
}