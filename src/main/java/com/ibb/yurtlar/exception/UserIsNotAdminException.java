package com.ibb.yurtlar.exception;

public class UserIsNotAdminException
        extends RuntimeException {

    public UserIsNotAdminException(Long userId) {
        super(
                userId
                        + " ID değerine sahip kullanıcının rolü ADMIN değildir."
        );
    }
}