package com.ibb.yurtlar.exception;

public class UserIsNotStudentException
        extends RuntimeException {

    public UserIsNotStudentException(Long userId) {
        super(
                userId
                        + " ID değerine sahip kullanıcının rolü STUDENT değildir."
        );
    }
}