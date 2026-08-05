package com.ibb.yurtlar.exception;

public class DormitoryAlreadyExistsException
        extends RuntimeException {

    public DormitoryAlreadyExistsException(String name) {
        super(
                "'" + name
                        + "' isimli yurt zaten bulunmaktadır."
        );
    }
}