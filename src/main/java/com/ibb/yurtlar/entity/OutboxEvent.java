package com.ibb.yurtlar.entity;

import com.ibb.yurtlar.enums.OutboxEventStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "outbox_events",
        indexes = {
                @Index(
                        name = "idx_outbox_status_created_id",
                        columnList = "status, created_at, id"
                )
        }
)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "event_id",
            nullable = false,
            unique = true,
            length = 36
    )
    private String eventId;

    @Column(
            name = "event_type",
            nullable = false,
            length = 100
    )
    private String eventType;

    @Column(
            name = "aggregate_type",
            nullable = false,
            length = 100
    )
    private String aggregateType;

    @Column(
            name = "aggregate_id",
            nullable = false
    )
    private Long aggregateId;

    @Column(
            name = "topic",
            nullable = false,
            length = 150
    )
    private String topic;

    @Column(
            name = "event_key",
            nullable = false,
            length = 150
    )
    private String eventKey;

    @Lob
    @Column(
            name = "payload",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private OutboxEventStatus status;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

}