package com.ibb.yurtlar.exception;

public class EmptyFileException extends RuntimeException {

    public EmptyFileException() {
        super("Yüklenecek dosya boş olamaz.");
    }
}