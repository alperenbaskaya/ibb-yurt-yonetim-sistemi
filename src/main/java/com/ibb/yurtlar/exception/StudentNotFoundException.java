package com.ibb.yurtlar.exception;

public class StudentNotFoundException
        extends RuntimeException {

    public StudentNotFoundException(Long id) {
        super(
                id
                        + " ID değerine sahip öğrenci bulunamadı."
        );
    }
}