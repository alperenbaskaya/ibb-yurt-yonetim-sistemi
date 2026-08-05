package com.ibb.yurtlar.exception;

public class InvalidFileSizeException
        extends RuntimeException {

    public InvalidFileSizeException() {
        super("Dosya boyutu en fazla 5 MB olabilir.");
    }
}