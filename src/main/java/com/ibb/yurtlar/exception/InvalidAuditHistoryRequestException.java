package com.ibb.yurtlar.exception;

public class InvalidAuditHistoryRequestException extends RuntimeException {
    public InvalidAuditHistoryRequestException(String message) {
        super(message);
    }
}
