package com.ibb.yurtlar.service;

import com.ibb.yurtlar.entity.ProcessedKafkaEvent;
import com.ibb.yurtlar.exception.DuplicateKafkaEventException;
import com.ibb.yurtlar.kafka.event.DocumentUploadedEvent;
import com.ibb.yurtlar.repository.ProcessedKafkaEventRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DocumentUploadedEventConsumerService {

    private static final String CONSUMER_NAME =
            "document-upload-idempotency-group-v1";

    private final ProcessedKafkaEventRepository
            processedKafkaEventRepository;

    public DocumentUploadedEventConsumerService(
            ProcessedKafkaEventRepository processedKafkaEventRepository
    ) {
        this.processedKafkaEventRepository =
                processedKafkaEventRepository;
    }

    @Transactional
    public void process(DocumentUploadedEvent event) {

        boolean alreadyProcessed =
                processedKafkaEventRepository
                        .existsByConsumerNameAndEventId(
                                CONSUMER_NAME,
                                event.eventId()
                        );

        if (alreadyProcessed) {
            throw new DuplicateKafkaEventException(
                    event.eventId(),
                    CONSUMER_NAME
            );
        }

        ProcessedKafkaEvent processedEvent =
                new ProcessedKafkaEvent();

        processedEvent.setEventId(
                event.eventId()
        );

        processedEvent.setConsumerName(
                CONSUMER_NAME
        );

        processedEvent.setEventType(
                event.eventType()
        );

        processedEvent.setProcessedAt(
                LocalDateTime.now()
        );

        try {

            /*
             * saveAndFlush kullanıyoruz.
             *
             * save() SQL INSERT'i transaction sonuna kadar
             * erteleyebilir.
             *
             * Biz unique constraint ihlalini TAM BURADA
             * görmek istediğimiz için flush ediyoruz.
             */
            processedKafkaEventRepository
                    .saveAndFlush(processedEvent);

        } catch (DataIntegrityViolationException exception) {

            /*
             * İki consumer aynı anda:
             *
             * exists -> false
             *
             * görmüş olabilir.
             *
             * DB unique constraint son savunma hattımızdır.
             */
            throw new DuplicateKafkaEventException(
                    event.eventId(),
                    CONSUMER_NAME
            );
        }

        /*
         * İleride gerçek side effect buraya gelecek.
         *
         * Örnek:
         * Elasticsearch indexle
         * Redis projection güncelle
         * başka bir tabloyu güncelle
         *
         * Bunlardan DB tabanlı olanlar aynı transaction
         * içerisinde yapılabilir.
         */
    }
}