package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.DocumentReview;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ibb.yurtlar.enums.DocumentReviewDecision;

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

    long countByReviewer_IdAndDecision(
            Long reviewerId,
            DocumentReviewDecision decision
    );

    List<DocumentReview>
    findTop10ByReviewer_IdOrderByReviewedAtDesc(
            Long reviewerId
    );
}