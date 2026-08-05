package com.ibb.yurtlar.exception;

public class ActiveAdmissionNotFoundException
        extends RuntimeException {

    public ActiveAdmissionNotFoundException(Long studentId) {
        super(
                studentId
                        + " ID değerine sahip öğrencinin aktif dönem için kabul kaydı bulunamadı."
        );
    }
}

//Öğrenci geçmiş dönemlerde kaldı ama bu dönem başvurmadı.
//Öğrencinin Student profili var ama henüz Admission oluşturulmadı.
//Aktif dönem bulunuyor ama öğrenci o döneme bağlanmadı.