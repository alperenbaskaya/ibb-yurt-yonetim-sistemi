package com.ibb.yurtlar.exception;

public class ActiveDormitoryTermNotFoundException
        extends RuntimeException {

    public ActiveDormitoryTermNotFoundException() {
        super("Aktif bir yurt dönemi bulunamadı.");
    }
}