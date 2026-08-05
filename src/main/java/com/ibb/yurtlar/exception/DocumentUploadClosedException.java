package com.ibb.yurtlar.exception;

public class DocumentUploadClosedException
        extends RuntimeException {

    public DocumentUploadClosedException() {
        super(
                "Belge yükleme tarih aralığı dışında olduğunuz için belge yüklenemez."
        );
    }
}