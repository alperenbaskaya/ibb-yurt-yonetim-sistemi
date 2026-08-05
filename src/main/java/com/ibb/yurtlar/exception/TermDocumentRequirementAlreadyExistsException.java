package com.ibb.yurtlar.exception;

public class TermDocumentRequirementAlreadyExistsException
        extends RuntimeException {

    public TermDocumentRequirementAlreadyExistsException(
            Long dormitoryTermId,
            Long documentTypeId
    ) {
        super(
                dormitoryTermId
                        + " ID değerine sahip dönem için "
                        + documentTypeId
                        + " ID değerine sahip belge türü zaten tanımlanmıştır."
        );
    }
}