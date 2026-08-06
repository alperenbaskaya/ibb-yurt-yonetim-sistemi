package com.ibb.yurtlar.exception;

public class UserManagementAccessDeniedException
        extends RuntimeException {

    public UserManagementAccessDeniedException(
            String message
    ) {
        super(message);
    }
}