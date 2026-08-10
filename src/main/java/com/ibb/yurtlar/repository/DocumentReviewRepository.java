package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.DocumentReview;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.ibb.yurtlar.dto.DormitoryReviewerWorkloadResponse;

import java.util.List;

public interface DocumentReviewRepository
        extends JpaRepository<DocumentReview, Long> {

    List<DocumentReview>
    findAllByStudentDocument_IdOrderByReviewedAtDesc(
            Long studentDocumentId
    );

    List<DocumentReview>
    findAllByReviewer_IdOrderByReviewedAtDesc(
            Long reviewerUserId
    );

    @Query("""
            SELECT dr
            FROM DocumentReview dr
            JOIN FETCH dr.reviewer reviewer
            JOIN FETCH dr.studentDocument document
            JOIN FETCH document.documentType documentType
            JOIN FETCH document.admission admission
            JOIN FETCH admission.student student
            JOIN FETCH student.user studentUser
            JOIN FETCH admission.dormitory dormitory
            JOIN FETCH admission.dormitoryTerm term
            WHERE dormitory.id = :dormitoryId
              AND term.active = true
            ORDER BY dr.reviewedAt DESC
            """)
    List<DocumentReview>
    findRecentByDormitoryAndActiveTerm(
            @Param("dormitoryId")
            Long dormitoryId,

            Pageable pageable
    );

    @Query("""
            SELECT dr
            FROM DocumentReview dr
            JOIN FETCH dr.studentDocument document
            JOIN FETCH document.documentType documentType
            JOIN FETCH document.admission admission
            WHERE admission.id = :admissionId
            ORDER BY dr.reviewedAt DESC
            """)
    List<DocumentReview>
    findRecentByAdmissionId(
            @Param("admissionId")
            Long admissionId,

            Pageable pageable
    );

    @Query("""
            SELECT review
            FROM DocumentReview review
            JOIN FETCH review.reviewer reviewer
            JOIN FETCH review.studentDocument document
            JOIN FETCH document.documentType documentType
            WHERE document.admission.id = :admissionId
            ORDER BY review.reviewedAt DESC
            """)
    List<DocumentReview> findAllForAdminInspectionByAdmissionId(
            @Param("admissionId") Long admissionId
    );

    @Query("""
        SELECT review
        FROM DocumentReview review
        JOIN FETCH review.studentDocument document
        JOIN FETCH document.documentType documentType
        WHERE document.admission.id = :admissionId
        ORDER BY review.reviewedAt ASC, review.id ASC
        """)
    List<DocumentReview> findAllForStudentTimelineByAdmissionId(
            @Param("admissionId") Long admissionId
    );

    @Query("""
        SELECT new com.ibb.yurtlar.dto.DormitoryReviewerWorkloadResponse(
            reviewer.id,
            reviewer.firstName,
            reviewer.lastName,
            reviewer.email,
            reviewer.active,

            SUM(
                CASE
                    WHEN review.decision =
                         com.ibb.yurtlar.enums.DocumentReviewDecision.APPROVED
                    THEN 1
                    ELSE 0
                END
            ),

            SUM(
                CASE
                    WHEN review.decision =
                         com.ibb.yurtlar.enums.DocumentReviewDecision.REJECTED
                    THEN 1
                    ELSE 0
                END
            ),

            SUM(
                CASE
                    WHEN review.decision =
                         com.ibb.yurtlar.enums.DocumentReviewDecision.REVISION_REQUIRED
                    THEN 1
                    ELSE 0
                END
            ),

            COUNT(review)
        )
        FROM AppUser reviewer
        LEFT JOIN DocumentReview review
               ON review.reviewer = reviewer
              AND review.studentDocument.admission.dormitoryTerm.id =
                  :activeTermId
              AND review.studentDocument.admission.dormitory.id =
                  :dormitoryId
        WHERE reviewer.role =
              com.ibb.yurtlar.enums.Role.REVIEWER
          AND reviewer.dormitory.id = :dormitoryId
        GROUP BY reviewer.id,
                 reviewer.firstName,
                 reviewer.lastName,
                 reviewer.email,
                 reviewer.active
        ORDER BY COUNT(review) DESC,
                 reviewer.firstName ASC,
                 reviewer.lastName ASC
        """)
    List<DormitoryReviewerWorkloadResponse>
    findReviewerWorkloads(
            @Param("activeTermId")
            Long activeTermId,

            @Param("dormitoryId")
            Long dormitoryId
    );
}
