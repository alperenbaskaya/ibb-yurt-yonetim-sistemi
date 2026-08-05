package com.ibb.yurtlar.exception;

public class InactiveDormitoryException
        extends RuntimeException {

    public InactiveDormitoryException(Long dormitoryId) {
        super(
                dormitoryId
                        + " ID değerine sahip yurt pasif olduğu için kullanıcıya atanamaz."
        );
    }
}