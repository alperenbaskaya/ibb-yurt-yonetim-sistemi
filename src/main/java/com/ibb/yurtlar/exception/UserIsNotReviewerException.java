package com.ibb.yurtlar.exception;

public class UserIsNotReviewerException
        extends RuntimeException {

    public UserIsNotReviewerException(Long userId) {
        super(
                userId
                        + " ID değerine sahip kullanıcının rolü REVIEWER değildir."
        );
    }
}