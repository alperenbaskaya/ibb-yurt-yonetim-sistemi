package com.ibb.yurtlar.exception;

public class DocumentNotRequiredForTermException
        extends RuntimeException {

    public DocumentNotRequiredForTermException(
            Long termId,
            Long documentTypeId
    ) {
        super(
                documentTypeId
                        + " ID değerine sahip belge türü, "
                        + termId
                        + " ID değerine sahip dönem için tanımlanmamıştır."
        );
    }
}