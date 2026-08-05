package com.ibb.yurtlar.exception;

public class AdmissionNotFoundException
        extends RuntimeException {

    public AdmissionNotFoundException(Long id) {
        super(
                id
                        + " ID değerine sahip kabul kaydı bulunamadı."
        );
    }
}