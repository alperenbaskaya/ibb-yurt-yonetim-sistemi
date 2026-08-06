package com.ibb.yurtlar.exception;

public class ReviewerDormitoryAccessDeniedException
        extends RuntimeException {

    public ReviewerDormitoryAccessDeniedException(
            Long studentDocumentId
    ) {
        super(
                "Bu belgeyi değerlendirme yetkiniz bulunmamaktadır. "
                        + "Belge ID: "
                        + studentDocumentId
        );
    }
}