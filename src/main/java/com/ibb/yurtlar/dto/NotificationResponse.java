package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.NotificationReferenceType;
import com.ibb.yurtlar.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(

        Long id,

        NotificationType type,

        String title,
        String message,

        boolean read,

        NotificationReferenceType referenceType,
        Long referenceId,

        LocalDateTime createdAt

) {
}