package com.ibb.yurtlar.exception;

public class ActiveAdmissionNotFoundForCurrentStudentException
        extends RuntimeException {

    public ActiveAdmissionNotFoundForCurrentStudentException() {
        super(
                "Giriş yapan öğrenci için aktif döneme ait kabul kaydı bulunamadı."
        );
    }
}