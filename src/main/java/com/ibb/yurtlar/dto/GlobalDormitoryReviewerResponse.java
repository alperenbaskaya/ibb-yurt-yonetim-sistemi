package com.ibb.yurtlar.dto;

public record GlobalDormitoryReviewerResponse(

        Long reviewerId,
        String firstName,
        String lastName,
        String email,
        boolean active

) {
}