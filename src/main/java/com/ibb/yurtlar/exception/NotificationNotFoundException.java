package com.ibb.yurtlar.exception;

public class NotificationNotFoundException
        extends RuntimeException {

    public NotificationNotFoundException(Long notificationId) {
        super(
                "Bildirim bulunamadı. ID: "
                        + notificationId
        );
    }
}
