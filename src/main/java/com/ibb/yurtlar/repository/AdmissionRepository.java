package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.dto.AdmissionStatusCountResponse;
import com.ibb.yurtlar.entity.Admission;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ibb.yurtlar.enums.AdmissionStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.List;

public interface AdmissionRepository extends JpaRepository<Admission, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT admission
        FROM Admission admission
        WHERE admission.id = :admissionId
        """)
    Optional<Admission> findByIdForDocumentCompletionUpdate(
            @Param("admissionId") Long admissionId
    );

    boolean existsByDormitoryTerm_Id(Long dormitoryTermId);

    boolean existsByStudent_IdAndDormitoryTerm_Id(Long studentId, Long dormitoryTermId);

    List<Admission>
    findAllByStudent_IdOrderByDormitoryTerm_StartDateDesc(Long studentId);

    @Query("""
        SELECT a
        FROM Admission a
        WHERE (:termId IS NULL
               OR a.dormitoryTerm.id = :termId)
          AND (:status IS NULL
               OR a.status = :status)
          AND (:dormitoryName IS NULL
               OR LOWER(a.dormitory.name)
                    LIKE LOWER(CONCAT('%', :dormitoryName, '%')))
        ORDER BY a.dormitoryTerm.startDate DESC,
                 a.createdAt DESC
        """)
    List<Admission> findByFilters(
            @Param("termId") Long termId,
            @Param("status") AdmissionStatus status,
            @Param("dormitoryName") String dormitoryName
    );

    long countByDormitoryTerm_IdAndStatus(
            Long dormitoryTermId,
            AdmissionStatus status
    );

    long countByDormitoryTerm_Id(Long dormitoryTermId);

    Optional<Admission> findByStudent_IdAndDormitoryTerm_ActiveTrue(
            Long studentId
    );

    List<Admission>
    findTop10ByDormitoryTerm_IdOrderByCreatedAtDesc(
            Long dormitoryTermId
    );


    long countByDormitoryTerm_IdAndDormitory_Id(
            Long dormitoryTermId,
            Long dormitoryId
    );

    long countByDormitoryTerm_IdAndDormitory_IdAndStatus(
            Long dormitoryTermId,
            Long dormitoryId,
            AdmissionStatus status
    );

    List<Admission>
    findTop10ByDormitoryTerm_IdAndDormitory_IdOrderByCreatedAtDesc(
            Long dormitoryTermId,
            Long dormitoryId
    );

    @Query("""
        SELECT a
        FROM Admission a
        JOIN FETCH a.student student
        JOIN FETCH student.user user
        JOIN FETCH a.dormitoryTerm term
        JOIN FETCH a.dormitory dormitory
        WHERE LOWER(TRIM(user.email))
              = LOWER(TRIM(:email))
          AND term.active = true
        """)
    Optional<Admission> findActiveAdmissionByStudentEmail(
            @Param("email") String email
    );

    @Query("""
        SELECT a
        FROM Admission a
        JOIN FETCH a.student student
        JOIN FETCH student.user studentUser
        JOIN FETCH a.dormitoryTerm term
        JOIN FETCH a.dormitory dormitory
        WHERE (:termId IS NULL
               OR term.id = :termId)
          AND (:status IS NULL
               OR a.status = :status)
          AND (:dormitoryName IS NULL
               OR LOWER(dormitory.name)
                    LIKE LOWER(
                        CONCAT('%', :dormitoryName, '%')
                    ))
          AND (:adminDormitoryId IS NULL
               OR dormitory.id = :adminDormitoryId)
        ORDER BY term.startDate DESC,
                 a.createdAt DESC
        """)
    List<Admission> findByAdminScopeAndFilters(
            @Param("termId")
            Long termId,

            @Param("status")
            AdmissionStatus status,

            @Param("dormitoryName")
            String dormitoryName,

            @Param("adminDormitoryId")
            Long adminDormitoryId
    );

    @Query("""
        SELECT COUNT(a)
        FROM Admission a
        JOIN a.dormitoryTerm term
        JOIN a.dormitory dormitory
        WHERE term.id = :termId
          AND a.status = :status
          AND (:adminDormitoryId IS NULL
               OR dormitory.id = :adminDormitoryId)
        """)
    long countByTermAndStatusAndAdminScope(
            @Param("termId")
            Long termId,

            @Param("status")
            AdmissionStatus status,

            @Param("adminDormitoryId")
            Long adminDormitoryId
    );

    @Query("""
        SELECT a
        FROM Admission a
        JOIN FETCH a.student student
        JOIN FETCH student.user studentUser
        JOIN FETCH a.dormitory dormitory
        JOIN FETCH a.dormitoryTerm term
        WHERE dormitory.id = :dormitoryId
          AND term.active = true
          AND a.status = :status
        ORDER BY studentUser.firstName ASC,
                 studentUser.lastName ASC
        """)
    List<Admission> findActiveTermByDormitoryAndStatus(
            @Param("dormitoryId")
            Long dormitoryId,

            @Param("status")
            AdmissionStatus status
    );

    @Query(
            value = """
                SELECT a FROM Admission a
                JOIN FETCH a.student student
                JOIN FETCH student.user studentUser
                JOIN FETCH a.dormitoryTerm term
                JOIN FETCH a.dormitory dormitory
                WHERE term.id = :termId
                  AND (:status IS NULL OR a.status = :status)
                """,
            countQuery = """
                SELECT COUNT(a) FROM Admission a
                WHERE a.dormitoryTerm.id = :termId
                  AND (:status IS NULL OR a.status = :status)
                """
    )
    Page<Admission> findGlobalCurrentTermPage(
            @Param("termId") Long termId,
            @Param("status") AdmissionStatus status,
            Pageable pageable
    );

    @Query("""
        SELECT a FROM Admission a
        JOIN FETCH a.student student
        JOIN FETCH student.user studentUser
        JOIN FETCH a.dormitoryTerm term
        JOIN FETCH a.dormitory dormitory
        WHERE term.id = :termId AND a.status = :status
        ORDER BY a.createdAt DESC, a.id DESC
        """)
    List<Admission> findAllByTermIdAndStatus(
            @Param("termId") Long termId,
            @Param("status") AdmissionStatus status
    );

    @Query("""
        SELECT admission
        FROM Admission admission
        JOIN FETCH admission.student student
        JOIN FETCH student.user studentUser
        JOIN FETCH admission.dormitoryTerm term
        JOIN FETCH admission.dormitory dormitory
        WHERE student.id = :studentId
          AND dormitory.id = :dormitoryId
          AND term.id = :termId
          AND admission.status =
              com.ibb.yurtlar.enums.AdmissionStatus.APPROVED
        """)
    Optional<Admission> findApprovedReviewerStudentAdmission(
            @Param("studentId") Long studentId,
            @Param("dormitoryId") Long dormitoryId,
            @Param("termId") Long termId
    );

    @Query("""
        SELECT admission
        FROM Admission admission
        JOIN FETCH admission.student student
        JOIN FETCH student.user studentUser
        JOIN FETCH admission.dormitoryTerm term
        JOIN FETCH admission.dormitory dormitory
        WHERE student.id = :studentId
          AND term.id = :termId
          AND (:dormitoryId IS NULL OR dormitory.id = :dormitoryId)
        """)
    Optional<Admission> findAdminInspectionAdmission(
            @Param("studentId") Long studentId,
            @Param("termId") Long termId,
            @Param("dormitoryId") Long dormitoryId
    );

    @Query("""
        SELECT new com.ibb.yurtlar.dto.AdmissionStatusCountResponse(

            COUNT(admission),

            COUNT(
                CASE
                    WHEN admission.status =
                         com.ibb.yurtlar.enums.AdmissionStatus.PENDING
                    THEN 1
                    ELSE null
                END
            ),

            COUNT(
                CASE
                    WHEN admission.status =
                         com.ibb.yurtlar.enums.AdmissionStatus.APPROVED
                    THEN 1
                    ELSE null
                END
            ),

            COUNT(
                CASE
                    WHEN admission.status =
                         com.ibb.yurtlar.enums.AdmissionStatus.REJECTED
                    THEN 1
                    ELSE null
                END
            )
        )
        FROM Admission admission
        WHERE admission.dormitoryTerm.id = :activeTermId
          AND admission.dormitory.id = :dormitoryId
        """)
    AdmissionStatusCountResponse getStatusCountsForDormitory(
            @Param("activeTermId")
            Long activeTermId,

            @Param("dormitoryId")
            Long dormitoryId
    );

    @Query("""
        SELECT new com.ibb.yurtlar.dto.AdmissionStatusCountResponse(

            COUNT(admission),

            COUNT(
                CASE
                    WHEN admission.status =
                         com.ibb.yurtlar.enums.AdmissionStatus.PENDING
                    THEN 1
                    ELSE null
                END
            ),

            COUNT(
                CASE
                    WHEN admission.status =
                         com.ibb.yurtlar.enums.AdmissionStatus.APPROVED
                    THEN 1
                    ELSE null
                END
            ),

            COUNT(
                CASE
                    WHEN admission.status =
                         com.ibb.yurtlar.enums.AdmissionStatus.REJECTED
                    THEN 1
                    ELSE null
                END
            )
        )
        FROM Admission admission
        WHERE admission.dormitoryTerm.id = :activeTermId
        """)
    AdmissionStatusCountResponse
    getGlobalStatusCountsForActiveTerm(
            @Param("activeTermId")
            Long activeTermId
    );




}
