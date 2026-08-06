package com.ibb.yurtlar.exception;

public class AdmissionAccessDeniedException
        extends RuntimeException {

    public AdmissionAccessDeniedException(
            Long admissionId
    ) {
        super(
                "Bu kabul kaydına erişim yetkiniz bulunmamaktadır. "
                        + "Kabul ID: "
                        + admissionId
        );
    }

    public AdmissionAccessDeniedException(
            String message
    ) {
        super(message);
    }
}