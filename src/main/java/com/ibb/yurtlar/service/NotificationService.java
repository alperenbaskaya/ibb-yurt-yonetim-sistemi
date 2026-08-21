package com.ibb.yurtlar.service;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.*;

import com.ibb.yurtlar.exception.BusinessException;

import com.ibb.yurtlar.dto.NotificationResponse;
import com.ibb.yurtlar.dto.NotificationUnreadCountResponse;
import com.ibb.yurtlar.dto.NotificationPageResponse;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Notification;
import com.ibb.yurtlar.enums.NotificationReferenceType;
import com.ibb.yurtlar.enums.NotificationType;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.NotificationRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

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
                                () -> new BusinessException(USER_NOT_FOUND,
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
    public NotificationResponse createNotification(
            AppUser recipient,
            NotificationType type,
            String title,
            String message,
            NotificationReferenceType referenceType,
            Long referenceId
    ) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        return toResponse(notificationRepository.save(notification));
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
    public NotificationPageResponse
    getMyNotifications(
            String email, int page, int size
    ) {
        AppUser authenticatedUser =
                findAuthenticatedUser(
                        email
                );

        validatePage(page, size);
        return toPageResponse(notificationRepository
                .findAllForRecipient(
                        authenticatedUser.getId(), PageRequest.of(page, size)
                ));
    }

    @Transactional(readOnly = true)
    public NotificationPageResponse
    getMyUnreadNotifications(
            String email, int page, int size
    ) {
        AppUser authenticatedUser =
                findAuthenticatedUser(
                        email
                );

        validatePage(page, size);
        return toPageResponse(notificationRepository
                .findUnreadForRecipient(
                        authenticatedUser.getId(), PageRequest.of(page, size)
                ));
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
                                () -> new BusinessException(NOTIFICATION_NOT_FOUND,
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

    private NotificationPageResponse toPageResponse(Page<Notification> notificationPage) {
        return new NotificationPageResponse(
                notificationPage.getContent().stream().map(this::toResponse).toList(),
                notificationPage.getNumber(), notificationPage.getSize(),
                notificationPage.getTotalElements(), notificationPage.getTotalPages(),
                notificationPage.isFirst(), notificationPage.isLast()
        );
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(INVALID_NOTIFICATION_PAGE_REQUEST,
                    "Sayfa 0 veya daha büyük, sayfa boyutu 1 ile 100 arasında olmalıdır."
            );
        }
    }
}
