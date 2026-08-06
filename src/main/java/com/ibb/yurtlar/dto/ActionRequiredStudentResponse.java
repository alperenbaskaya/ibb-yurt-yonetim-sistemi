package com.ibb.yurtlar.dto;

public record ActionRequiredStudentResponse(

        Long studentId,
        Long admissionId,

        String firstName,
        String lastName,

        int missingDocumentCount,
        int revisionRequiredDocumentCount,
        int rejectedDocumentCount

) {
}