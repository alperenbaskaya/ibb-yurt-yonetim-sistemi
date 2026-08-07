package com.ibb.yurtlar.exception;

import com.ibb.yurtlar.enums.StudentDocumentStatus;

public class InvalidDocumentReplacementStateException
        extends RuntimeException {

    public InvalidDocumentReplacementStateException(
            Long documentId,
            StudentDocumentStatus status
    ) {
        super(
                "Belge mevcut durumunda yeniden yüklenemez. Belge ID: "
                        + documentId
                        + ", durum: "
                        + status
        );
    }
}
