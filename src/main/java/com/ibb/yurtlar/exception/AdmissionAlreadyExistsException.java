package com.ibb.yurtlar.exception;

public class AdmissionAlreadyExistsException
        extends RuntimeException {

    public AdmissionAlreadyExistsException(
            Long studentId,
            Long dormitoryTermId
    ) {
        super(
                studentId
                        + " ID değerine sahip öğrenci için "
                        + dormitoryTermId
                        + " ID değerine sahip dönemde zaten bir kabul kaydı bulunmaktadır."
        );
    }
}