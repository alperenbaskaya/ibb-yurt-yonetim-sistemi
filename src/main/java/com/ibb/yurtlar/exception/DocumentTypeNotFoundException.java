package com.ibb.yurtlar.exception;

public class DocumentTypeNotFoundException
        extends RuntimeException {

    public DocumentTypeNotFoundException(Long id) {
        super(
                id
                        + " ID değerine sahip belge türü bulunamadı."
        );
    }
}