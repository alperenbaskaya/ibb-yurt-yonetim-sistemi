package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.ibb.yurtlar.enums.NotificationReferenceType;
import com.ibb.yurtlar.enums.NotificationType;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    @Query("""
            SELECT notification
            FROM Notification notification
            JOIN FETCH notification.recipient recipient
            WHERE recipient.id = :recipientUserId
            ORDER BY notification.createdAt DESC, notification.id DESC
            """)
    Page<Notification> findAllForRecipient(
            @Param("recipientUserId")
            Long recipientUserId,
            Pageable pageable
    );

    @Query("""
            SELECT notification
            FROM Notification notification
            JOIN FETCH notification.recipient recipient
            WHERE recipient.id = :recipientUserId
              AND notification.read = false
            ORDER BY notification.createdAt DESC, notification.id DESC
            """)
    Page<Notification> findUnreadForRecipient(
            @Param("recipientUserId")
            Long recipientUserId,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(notification)
            FROM Notification notification
            WHERE notification.recipient.id = :recipientUserId
              AND notification.read = false
            """)
    long countUnreadForRecipient(
            @Param("recipientUserId")
            Long recipientUserId
    );

    @Modifying
    @Query("""
            UPDATE Notification notification
            SET notification.read = true
            WHERE notification.recipient.id = :recipientUserId
              AND notification.read = false
            """)
    int markAllAsRead(
            @Param("recipientUserId")
            Long recipientUserId
    );

    @Query("""
        SELECT COUNT(notification) > 0
        FROM Notification notification
        WHERE notification.recipient.id = :recipientUserId
          AND notification.type = :type
          AND notification.referenceType = :referenceType
          AND notification.referenceId = :referenceId
        """)
    boolean existsNotification(
            @Param("recipientUserId")
            Long recipientUserId,

            @Param("type")
            NotificationType type,

            @Param("referenceType")
            NotificationReferenceType referenceType,

            @Param("referenceId")
            Long referenceId
    );
}
