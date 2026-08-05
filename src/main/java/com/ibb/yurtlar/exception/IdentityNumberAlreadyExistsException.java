package com.ibb.yurtlar.exception;

public class IdentityNumberAlreadyExistsException
        extends RuntimeException {

    public IdentityNumberAlreadyExistsException(
            String identityNumber
    ) {
        super(
                identityNumber
                        + " TC kimlik numarasıyla kayıtlı bir öğrenci zaten bulunmaktadır."
        );
    }
}