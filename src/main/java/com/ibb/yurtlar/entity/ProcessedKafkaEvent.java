package com.ibb.yurtlar.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "processed_kafka_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_processed_event_consumer",
                        columnNames = {
                                "consumer_name",
                                "event_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_processed_event_created",
                        columnList = "processed_at"
                )
        }
)
public class ProcessedKafkaEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "event_id",
            nullable = false,
            length = 36
    )
    private String eventId;

    @Column(
            name = "consumer_name",
            nullable = false,
            length = 120
    )
    private String consumerName;

    @Column(
            name = "event_type",
            nullable = false,
            length = 100
    )
    private String eventType;

    @Column(
            name = "processed_at",
            nullable = false
    )
    private LocalDateTime processedAt;

    public ProcessedKafkaEvent() {
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}