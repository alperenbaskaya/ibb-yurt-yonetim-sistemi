package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.StudentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ibb.yurtlar.enums.StudentDocumentStatus;

import java.util.List;
import java.util.Optional;

public interface StudentDocumentRepository
        extends JpaRepository<StudentDocument, Long> {

    Optional<StudentDocument>
    findByAdmission_IdAndDocumentType_Id(
            Long admissionId,
            Long documentTypeId
    );

    List<StudentDocument>
    findAllByAdmission_IdOrderByDocumentType_NameAsc(
            Long admissionId
    );


    List<StudentDocument>
    findAllByStatusOrderByUploadedAtAsc(
            StudentDocumentStatus status
    );

    long countByStatus(
            StudentDocumentStatus status
    );

    List<StudentDocument>
    findTop10ByStatusOrderByUploadedAtAsc(
            StudentDocumentStatus status
    );

    long countByAdmission_DormitoryTerm_IdAndStatus(
            Long dormitoryTermId,
            StudentDocumentStatus status
    );

    long countByAdmission_DormitoryTerm_IdAndAdmission_Dormitory_IdAndStatus(
            Long dormitoryTermId,
            Long dormitoryId,
            StudentDocumentStatus status
    );
}