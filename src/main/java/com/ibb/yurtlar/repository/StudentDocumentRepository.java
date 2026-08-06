package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.dto.DocumentStatusCountResponse;
import com.ibb.yurtlar.entity.StudentDocument;
import com.ibb.yurtlar.enums.StudentDocumentStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.ibb.yurtlar.enums.AdmissionStatus;
import com.ibb.yurtlar.dto.PendingDocumentTypeCountResponse;

import java.util.List;
import java.util.Optional;

public interface StudentDocumentRepository
        extends JpaRepository<StudentDocument, Long> {

    Optional<StudentDocument>
    findByAdmission_IdAndDocumentType_Id(
            Long admissionId,
            Long documentTypeId
    );

    @Query("""
            SELECT sd
            FROM StudentDocument sd
            JOIN FETCH sd.documentType dt
            WHERE sd.admission.id = :admissionId
            ORDER BY dt.name ASC
            """)
    List<StudentDocument> findAllByAdmissionId(
            @Param("admissionId")
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

    @Query("""
            SELECT COUNT(sd)
            FROM StudentDocument sd
            JOIN sd.admission a
            JOIN a.dormitoryTerm dt
            WHERE dt.id = :dormitoryTermId
              AND sd.status = :status
            """)
    long countByTermAndStatus(
            @Param("dormitoryTermId")
            Long dormitoryTermId,

            @Param("status")
            StudentDocumentStatus status
    );

    @Query("""
            SELECT COUNT(sd)
            FROM StudentDocument sd
            JOIN sd.admission a
            JOIN a.dormitoryTerm dt
            JOIN a.dormitory d
            WHERE dt.id = :dormitoryTermId
              AND d.id = :dormitoryId
              AND sd.status = :status
            """)
    long countByTermAndDormitoryAndStatus(
            @Param("dormitoryTermId")
            Long dormitoryTermId,

            @Param("dormitoryId")
            Long dormitoryId,

            @Param("status")
            StudentDocumentStatus status
    );

    @Query("""
            SELECT COUNT(sd)
            FROM StudentDocument sd
            JOIN sd.admission a
            JOIN a.dormitory d
            WHERE d.id = :dormitoryId
              AND sd.status = :status
            """)
    long countByDormitoryAndStatus(
            @Param("dormitoryId")
            Long dormitoryId,

            @Param("status")
            StudentDocumentStatus status
    );

    @Query("""
            SELECT sd
            FROM StudentDocument sd
            JOIN FETCH sd.admission a
            JOIN FETCH a.student s
            JOIN FETCH s.user su
            JOIN FETCH a.dormitory d
            JOIN FETCH sd.documentType dt
            WHERE d.id = :dormitoryId
              AND sd.status = :status
            ORDER BY sd.uploadedAt ASC
            """)
    List<StudentDocument> findByDormitoryAndStatus(
            @Param("dormitoryId")
            Long dormitoryId,

            @Param("status")
            StudentDocumentStatus status
    );

    @Query("""
            SELECT sd
            FROM StudentDocument sd
            JOIN FETCH sd.admission a
            JOIN FETCH a.student s
            JOIN FETCH s.user su
            JOIN FETCH a.dormitory d
            JOIN FETCH sd.documentType dt
            WHERE d.id = :dormitoryId
              AND sd.status = :status
            ORDER BY sd.uploadedAt ASC
            """)
    List<StudentDocument> findByDormitoryAndStatus(
            @Param("dormitoryId")
            Long dormitoryId,

            @Param("status")
            StudentDocumentStatus status,

            Pageable pageable
    );

    @Query("""
        SELECT COUNT(sd)
        FROM StudentDocument sd
        JOIN sd.admission a
        JOIN a.dormitory d
        JOIN a.dormitoryTerm dt
        WHERE d.id = :dormitoryId
          AND dt.active = true
          AND sd.status = :status
        """)
    long countByActiveTermAndDormitoryAndStatus(
            @Param("dormitoryId")
            Long dormitoryId,

            @Param("status")
            StudentDocumentStatus status
    );

    @Query("""
        SELECT sd
        FROM StudentDocument sd
        JOIN FETCH sd.admission a
        JOIN FETCH a.student s
        JOIN FETCH s.user su
        JOIN FETCH a.dormitory d
        JOIN FETCH a.dormitoryTerm term
        JOIN FETCH sd.documentType dt
        WHERE d.id = :dormitoryId
          AND term.active = true
          AND sd.status = :status
        ORDER BY sd.uploadedAt ASC
        """)
    List<StudentDocument>
    findOldestByActiveTermAndDormitoryAndStatus(
            @Param("dormitoryId")
            Long dormitoryId,

            @Param("status")
            StudentDocumentStatus status,

            Pageable pageable
    );

    @Query("""
        SELECT sd
        FROM StudentDocument sd
        JOIN FETCH sd.admission admission
        JOIN FETCH admission.student student
        JOIN FETCH student.user studentUser
        JOIN FETCH admission.dormitory dormitory
        JOIN FETCH admission.dormitoryTerm term
        JOIN FETCH sd.documentType documentType
        WHERE dormitory.id = :dormitoryId
          AND term.active = true
          AND sd.status = :status
        ORDER BY sd.uploadedAt ASC
        """)
    List<StudentDocument>
    findAllByActiveTermAndDormitoryAndStatus(
            @Param("dormitoryId")
            Long dormitoryId,

            @Param("status")
            StudentDocumentStatus status
    );

    @Query("""
        SELECT sd
        FROM StudentDocument sd
        JOIN FETCH sd.documentType documentType
        JOIN FETCH sd.admission admission
        JOIN FETCH admission.student student
        JOIN FETCH student.user studentUser
        JOIN FETCH admission.dormitory dormitory
        JOIN FETCH admission.dormitoryTerm term
        WHERE dormitory.id = :dormitoryId
          AND term.active = true
          AND admission.status = :admissionStatus
        """)
    List<StudentDocument> findAllForActiveTermDormitoryStudents(
            @Param("dormitoryId")
            Long dormitoryId,

            @Param("admissionStatus")
            AdmissionStatus admissionStatus
    );

    @Query("""
        SELECT new com.ibb.yurtlar.dto.PendingDocumentTypeCountResponse(
            documentType.id,
            documentType.name,
            COUNT(sd)
        )
        FROM StudentDocument sd
        JOIN sd.documentType documentType
        JOIN sd.admission admission
        JOIN admission.dormitory dormitory
        JOIN admission.dormitoryTerm term
        WHERE dormitory.id = :dormitoryId
          AND term.active = true
          AND sd.status = :status
        GROUP BY documentType.id,
                 documentType.name
        ORDER BY COUNT(sd) DESC,
                 documentType.name ASC
        """)
    List<PendingDocumentTypeCountResponse>
    findPendingDocumentCountsByType(
            @Param("dormitoryId")
            Long dormitoryId,

            @Param("status")
            StudentDocumentStatus status
    );

    @Query("""
        SELECT new com.ibb.yurtlar.dto.DocumentStatusCountResponse(

            COUNT(
                CASE
                    WHEN document.status =
                         com.ibb.yurtlar.enums.StudentDocumentStatus.UPLOADED
                    THEN 1
                    ELSE null
                END
            ),

            COUNT(
                CASE
                    WHEN document.status =
                         com.ibb.yurtlar.enums.StudentDocumentStatus.APPROVED
                    THEN 1
                    ELSE null
                END
            ),

            COUNT(
                CASE
                    WHEN document.status =
                         com.ibb.yurtlar.enums.StudentDocumentStatus.REJECTED
                    THEN 1
                    ELSE null
                END
            ),

            COUNT(
                CASE
                    WHEN document.status =
                         com.ibb.yurtlar.enums.StudentDocumentStatus.REVISION_REQUIRED
                    THEN 1
                    ELSE null
                END
            )
        )
        FROM StudentDocument document
        WHERE document.admission.dormitoryTerm.id = :activeTermId
          AND document.admission.dormitory.id = :dormitoryId
        """)
    DocumentStatusCountResponse getStatusCountsForDormitory(
            @Param("activeTermId")
            Long activeTermId,

            @Param("dormitoryId")
            Long dormitoryId
    );

    @Query("""
        SELECT new com.ibb.yurtlar.dto.DocumentStatusCountResponse(

            COUNT(
                CASE
                    WHEN document.status =
                         com.ibb.yurtlar.enums.StudentDocumentStatus.UPLOADED
                    THEN 1
                    ELSE null
                END
            ),

            COUNT(
                CASE
                    WHEN document.status =
                         com.ibb.yurtlar.enums.StudentDocumentStatus.APPROVED
                    THEN 1
                    ELSE null
                END
            ),

            COUNT(
                CASE
                    WHEN document.status =
                         com.ibb.yurtlar.enums.StudentDocumentStatus.REJECTED
                    THEN 1
                    ELSE null
                END
            ),

            COUNT(
                CASE
                    WHEN document.status =
                         com.ibb.yurtlar.enums.StudentDocumentStatus.REVISION_REQUIRED
                    THEN 1
                    ELSE null
                END
            )
        )
        FROM StudentDocument document
        WHERE document.admission.dormitoryTerm.id = :activeTermId
        """)
    DocumentStatusCountResponse
    getGlobalStatusCountsForActiveTerm(
            @Param("activeTermId")
            Long activeTermId
    );
}