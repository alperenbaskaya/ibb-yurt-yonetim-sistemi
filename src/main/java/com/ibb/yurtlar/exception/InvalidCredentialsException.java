package com.ibb.yurtlar.exception;

public class InvalidCredentialsException
        extends RuntimeException {

    public InvalidCredentialsException() {
        super("E-posta veya şifre hatalı.");
    }

}