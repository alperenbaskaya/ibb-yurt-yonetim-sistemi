package com.ibb.yurtlar.exception;

public class AdmissionNotApprovedException
        extends RuntimeException {

    public AdmissionNotApprovedException(Long admissionId) {
        super(
                admissionId
                        + " ID değerine sahip kabul kaydı onaylanmadığı için belge yüklenemez."
        );
    }
}