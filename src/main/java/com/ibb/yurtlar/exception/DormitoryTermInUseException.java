package com.ibb.yurtlar.exception;

public class DormitoryTermInUseException extends RuntimeException {

    public DormitoryTermInUseException(Long id) {
        super(
                id + " ID değerine sahip yurt dönemi kullanıldığı için silinemez."
        );
    }
}