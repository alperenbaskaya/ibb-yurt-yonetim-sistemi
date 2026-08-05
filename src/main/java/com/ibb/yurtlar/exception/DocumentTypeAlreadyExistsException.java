package com.ibb.yurtlar.exception;

public class DocumentTypeAlreadyExistsException
        extends RuntimeException {

    public DocumentTypeAlreadyExistsException(String name) {
        super(
                "'" + name
                        + "' isimli belge türü zaten bulunmaktadır."
        );
    }
}