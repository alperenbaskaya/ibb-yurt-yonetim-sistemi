package com.ibb.yurtlar.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super(email + " e-posta adresiyle kayıtlı bir kullanıcı zaten bulunmaktadır.");
    }
}