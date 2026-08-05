package com.ibb.yurtlar.exception;

public class FileStorageException extends RuntimeException {

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}