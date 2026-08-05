package com.ibb.yurtlar.exception;

public class InvalidUserConfigurationException
        extends RuntimeException {

    public InvalidUserConfigurationException(
            String message
    ) {
        super(message);
    }
}