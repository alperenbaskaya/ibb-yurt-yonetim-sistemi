package com.ibb.yurtlar.dto;

import java.time.LocalDateTime;

public record DocumentTypeResponse(

        Long id,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}