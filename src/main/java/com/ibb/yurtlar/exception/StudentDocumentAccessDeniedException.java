package com.ibb.yurtlar.exception;

public class StudentDocumentAccessDeniedException
        extends RuntimeException {

    public StudentDocumentAccessDeniedException(
            Long documentId
    ) {
        super(
                "Bu belgeye erişim yetkiniz bulunmamaktadır. "
                        + "Belge ID: "
                        + documentId
        );
    }
}