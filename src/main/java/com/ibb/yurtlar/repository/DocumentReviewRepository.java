package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.DocumentReview;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}