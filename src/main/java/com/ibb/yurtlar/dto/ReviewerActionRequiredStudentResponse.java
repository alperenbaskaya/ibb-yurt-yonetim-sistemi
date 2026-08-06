package com.ibb.yurtlar.dto;

public record ReviewerActionRequiredStudentResponse(

        Long studentId,
        Long admissionId,

        String firstName,
        String lastName,

        int missingDocumentCount,
        int revisionRequiredDocumentCount,
        int rejectedDocumentCount

) {
}