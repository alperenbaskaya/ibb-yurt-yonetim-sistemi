package com.ibb.yurtlar.exception;

public class StudentProfileAlreadyExistsException
        extends RuntimeException {

    public StudentProfileAlreadyExistsException(Long userId) {
        super(
                userId
                        + " ID değerine sahip kullanıcıya ait öğrenci profili zaten bulunmaktadır."
        );
    }
}