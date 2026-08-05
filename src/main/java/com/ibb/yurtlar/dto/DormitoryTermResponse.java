package com.ibb.yurtlar.dto;

import java.time.LocalDate;

public record DormitoryTermResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate documentUploadStartDate,
        LocalDate documentUploadEndDate,
        boolean active
) {
}