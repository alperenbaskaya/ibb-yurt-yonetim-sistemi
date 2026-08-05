package com.ibb.yurtlar.exception;

public class DormitoryTermNotFoundException extends RuntimeException {

    public DormitoryTermNotFoundException(Long id) {
        super(id + " ID değerine sahip yurt dönemi bulunamadı.");
    }
}