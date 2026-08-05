package com.ibb.yurtlar.exception;

public class DormitoryTermAlreadyExistsException extends RuntimeException {

    public DormitoryTermAlreadyExistsException(String termName) {
        super(termName + " isimli yurt dönemi zaten bulunmaktadır.");
    }
}