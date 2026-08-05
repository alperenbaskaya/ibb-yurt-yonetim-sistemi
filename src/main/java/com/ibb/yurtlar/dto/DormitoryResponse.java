package com.ibb.yurtlar.dto;

import java.time.LocalDateTime;

public record DormitoryResponse(

        Long id,
        String name,
        String address,
        Integer capacity,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}