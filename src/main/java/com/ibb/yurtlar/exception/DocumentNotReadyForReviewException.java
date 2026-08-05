package com.ibb.yurtlar.exception;

public class DocumentNotReadyForReviewException
        extends RuntimeException {

    public DocumentNotReadyForReviewException(Long documentId) {
        super(
                documentId
                        + " ID değerine sahip belge değerlendirme beklememektedir."
        );
    }
}