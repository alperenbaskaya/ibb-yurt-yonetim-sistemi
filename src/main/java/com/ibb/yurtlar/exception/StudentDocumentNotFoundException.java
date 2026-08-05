package com.ibb.yurtlar.exception;

public class StudentDocumentNotFoundException
        extends RuntimeException {

    public StudentDocumentNotFoundException(Long id) {
        super(
                id
                        + " ID değerine sahip öğrenci belgesi bulunamadı."
        );
    }
}