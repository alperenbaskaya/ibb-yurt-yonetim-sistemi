package com.ibb.yurtlar.dto;

import com.ibb.yurtlar.enums.StudentDocumentStatus;

import java.time.LocalDateTime;

public record StudentDocumentResponse(

        Long id,

        Long admissionId,
        Long studentId,
        String studentFirstName,
        String studentLastName,

        Long dormitoryTermId,
        String dormitoryTermName,

        Long documentTypeId,
        String documentTypeName,

        String originalFileName,
        String contentType,
        Long fileSize,

        StudentDocumentStatus status,
        LocalDateTime uploadedAt,
        LocalDateTime updatedAt


        /*  storedFileName  Alanlarını dışarı göndermiyoruz. Çünkü bunlar sunucunun iç yapısına aittir.
            filePath                                        */

) {

}