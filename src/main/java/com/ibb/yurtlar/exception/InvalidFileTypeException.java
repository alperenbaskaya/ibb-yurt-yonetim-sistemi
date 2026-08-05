package com.ibb.yurtlar.exception;

public class InvalidFileTypeException extends RuntimeException {

    public InvalidFileTypeException(String contentType) {
        super(
                "Desteklenmeyen dosya türü: "
                        + contentType
                        + ". Yalnızca PDF, JPEG ve PNG dosyaları yüklenebilir."
        );
    }
}