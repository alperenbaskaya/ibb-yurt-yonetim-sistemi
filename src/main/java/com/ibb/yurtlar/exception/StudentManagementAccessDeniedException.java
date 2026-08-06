package com.ibb.yurtlar.exception;

public class StudentManagementAccessDeniedException
        extends RuntimeException {

    public StudentManagementAccessDeniedException(
            Long studentId
    ) {
        super(
                "Bu öğrenciye erişim yetkiniz bulunmamaktadır. "
                        + "Öğrenci ID: "
                        + studentId
        );
    }

    public StudentManagementAccessDeniedException(
            String message
    ) {
        super(message);
    }
}