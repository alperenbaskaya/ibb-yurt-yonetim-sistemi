package com.ibb.yurtlar.enums;

public enum OutboxEventStatus {

    PENDING, //PENDING → MySQL’e kaydedildi ama Kafka’ya başarılı gönderim henüz tamamlanmadı.
    PUBLISHED //PUBLISHED → Kafka’ya gönderildi.
}