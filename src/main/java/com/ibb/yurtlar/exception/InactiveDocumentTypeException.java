package com.ibb.yurtlar.exception;

public class InactiveDocumentTypeException
        extends RuntimeException {

    public InactiveDocumentTypeException(Long documentTypeId) {
        super(
                documentTypeId
                        + " ID değerine sahip belge türü pasiftir ve yeni bir döneme eklenemez."
        );
    }
}