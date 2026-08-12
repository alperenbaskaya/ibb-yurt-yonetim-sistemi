package com.ibb.yurtlar.exception;

public class InvalidAdmissionRequestException extends RuntimeException {
    public InvalidAdmissionRequestException(String message) {
        super(message);
    }
}
