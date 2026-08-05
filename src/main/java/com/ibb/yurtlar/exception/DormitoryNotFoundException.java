package com.ibb.yurtlar.exception;

public class DormitoryNotFoundException
        extends RuntimeException {

    public DormitoryNotFoundException(Long id) {
        super(
                id
                        + " ID değerine sahip yurt bulunamadı."
        );
    }
}