package com.ibb.yurtlar.mapper;

import com.ibb.yurtlar.dto.StudentDocumentResponse;
import com.ibb.yurtlar.entity.Admission;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.DocumentType;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.entity.Student;
import com.ibb.yurtlar.entity.StudentDocument;
import org.springframework.stereotype.Component;

@Component
public class StudentDocumentMapper {

    public StudentDocumentResponse toResponse(
            StudentDocument document
    ) {
        Admission admission =
                document.getAdmission();

        Student student =
                admission.getStudent();

        AppUser user =
                student.getUser();

        DormitoryTerm term =
                admission.getDormitoryTerm();

        DocumentType documentType =
                document.getDocumentType();

        return new StudentDocumentResponse(
                document.getId(),

                admission.getId(),
                student.getId(),
                user.getFirstName(),
                user.getLastName(),

                term.getId(),
                term.getName(),

                documentType.getId(),
                documentType.getName(),

                document.getOriginalFileName(),
                document.getContentType(),
                document.getFileSize(),

                document.getStatus(),
                document.getUploadedAt(),
                document.getUpdatedAt()
        );
    }
}