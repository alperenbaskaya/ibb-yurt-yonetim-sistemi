package com.ibb.yurtlar.exception;

public class InactiveDormitoryTermException
        extends RuntimeException {

    public InactiveDormitoryTermException(Long termId) {
        super(
                termId
                        + " ID değerine sahip yurt dönemi aktif değildir."
        );
    }
}