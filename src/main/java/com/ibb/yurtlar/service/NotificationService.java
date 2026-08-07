package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.NotificationResponse;
import com.ibb.yurtlar.dto.NotificationUnreadCountResponse;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Notification;
import com.ibb.yurtlar.enums.NotificationReferenceType;
import com.ibb.yurtlar.enums.NotificationType;
import com.ibb.yurtlar.exception.UserNotFoundException;
import com.ibb.yurtlar.exception.NotificationNotFoundException;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.NotificationRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository
            notificationRepository;

    private final AppUserRepository
            appUserRepository;


    public NotificationService(
            NotificationRepository notificationRepository,
            AppUserRepository appUserRepository
    ) {
        this.notificationRepository =
                notificationRepository;

        this.appUserRepository =
                appUserRepository;

    }

    @Transactional
    public NotificationResponse createNotification(
            Long recipientUserId,
            NotificationType type,
            String title,
            String message,
            NotificationReferenceType referenceType,
            Long referenceId
    ) {
        AppUser recipient =
                appUserRepository
                        .findById(
                                recipientUserId
                        )
                        .orElseThrow(
                                () -> new UserNotFoundException(
                                        recipientUserId
                                )
                        );

        Notification notification =
                new Notification();

        notification.setRecipient(
                recipient
        );

        notification.setType(
                type
        );

        notification.setTitle(
                title
        );

        notification.setMessage(
                message
        );

        notification.setReferenceType(
                referenceType
        );

        notification.setReferenceId(
                referenceId
        );

        Notification savedNotification =
                notificationRepository.save(
                        notification
                );

        return toResponse(
                savedNotification
        );
    }

    @Transactional
    public NotificationResponse createNotificationIfAbsent(
            Long recipientUserId,
            NotificationType type,
            String title,
            String message,
            NotificationReferenceType referenceType,
            Long referenceId
    ) {
        boolean alreadyExists =
                notificationRepository
                        .existsNotification(
                                recipientUserId,
                                type,
                                referenceType,
                                referenceId
                        );

        if (alreadyExists) {
            return null;
        }

        return createNotification(
                recipientUserId,
                type,
                title,
                message,
                referenceType,
                referenceId
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse>
    getMyNotifications(
            String email
    ) {
        AppUser authenticatedUser =
                findAuthenticatedUser(
                        email
                );

        return notificationRepository
                .findAllForRecipient(
                        authenticatedUser.getId()
                )
                .stream()
                .map(
                        this::toResponse
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse>
    getMyUnreadNotifications(
            String email
    ) {
        AppUser authenticatedUser =
                findAuthenticatedUser(
                        email
                );

        return notificationRepository
                .findUnreadForRecipient(
                        authenticatedUser.getId()
                )
                .stream()
                .map(
                        this::toResponse
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificationUnreadCountResponse
    getMyUnreadCount(
            String email
    ) {
        AppUser authenticatedUser =
                findAuthenticatedUser(
                        email
                );

        long unreadCount =
                notificationRepository
                        .countUnreadForRecipient(
                                authenticatedUser.getId()
                        );

        return new NotificationUnreadCountResponse(
                unreadCount
        );
    }

    @Transactional
    public NotificationResponse markAsRead(
            Long notificationId,
            String email
    ) {
        AppUser authenticatedUser =
                findAuthenticatedUser(
                        email
                );

        Notification notification =
                notificationRepository
                        .findById(
                                notificationId
                        )
                        .orElseThrow(
                                () -> new NotificationNotFoundException(
                                        notificationId
                                )
                        );

        validateNotificationOwner(
                notification,
                authenticatedUser
        );

        if (!notification.isRead()) {
            notification.setRead(
                    true
            );
        }

        return toResponse(
                notification
        );
    }

    @Transactional
    public void markAllAsRead(
            String email
    ) {
        AppUser authenticatedUser =
                findAuthenticatedUser(
                        email
                );

        notificationRepository
                .markAllAsRead(
                        authenticatedUser.getId()
                );
    }

    private AppUser findAuthenticatedUser(
            String email
    ) {
        return appUserRepository
                .findByNormalizedEmail(
                        email
                )
                .orElseThrow(
                        () -> new UsernameNotFoundException(
                                "Giriş yapan kullanıcı bulunamadı."
                        )
                );
    }

    private void validateNotificationOwner(
            Notification notification,
            AppUser authenticatedUser
    ) {
        Long recipientUserId =
                notification
                        .getRecipient()
                        .getId();

        if (!recipientUserId.equals(
                authenticatedUser.getId()
        )) {
            throw new AccessDeniedException(
                    "Bu bildirime erişim yetkiniz bulunmamaktadır."
            );
        }
    }

    private NotificationResponse toResponse(
            Notification notification
    ) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),

                notification.getTitle(),
                notification.getMessage(),

                notification.isRead(),

                notification.getReferenceType(),
                notification.getReferenceId(),

                notification.getCreatedAt()
        );
    }
}
