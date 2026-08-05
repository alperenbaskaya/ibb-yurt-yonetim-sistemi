package com.ibb.yurtlar.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super(id + " ID değerine sahip kullanıcı bulunamadı.");
    }
}