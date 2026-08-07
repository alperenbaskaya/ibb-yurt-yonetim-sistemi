package com.ibb.yurtlar.entity;

import com.ibb.yurtlar.enums.NotificationReferenceType;
import com.ibb.yurtlar.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(
                        name = "idx_notifications_recipient",
                        columnList = "recipient_user_id"
                ),
                @Index(
                        name = "idx_notifications_recipient_read",
                        columnList = "recipient_user_id, is_read"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "recipient_user_id",
            nullable = false
    )
    private AppUser recipient;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 50
    )
    private NotificationType type;

    @Column(
            nullable = false,
            length = 150
    )
    private String title;

    @Column(
            nullable = false,
            length = 1000
    )
    private String message;

    @Column(
            name = "is_read",
            nullable = false
    )
    private boolean read;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "reference_type",
            length = 50
    )
    private NotificationReferenceType referenceType;

    @Column(
            name = "reference_id"
    )
    private Long referenceId;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {

        createdAt =
                LocalDateTime.now();

        read = false;
    }
}