package com.ibb.yurtlar.exception;

public class InvalidNotificationPageRequestException extends RuntimeException {
    public InvalidNotificationPageRequestException(String message) {
        super(message);
    }
}
