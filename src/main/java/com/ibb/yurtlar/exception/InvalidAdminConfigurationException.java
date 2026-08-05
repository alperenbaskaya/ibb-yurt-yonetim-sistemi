package com.ibb.yurtlar.exception;

public class InvalidAdminConfigurationException
        extends RuntimeException {

    public InvalidAdminConfigurationException(
            String message
    ) {
        super(message);
    }
}