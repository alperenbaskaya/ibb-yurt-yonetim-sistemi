package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.CreateDocumentReviewRequest;
import com.ibb.yurtlar.dto.DocumentReviewResponse;
import com.ibb.yurtlar.entity.Admission;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.DocumentReview;
import com.ibb.yurtlar.entity.Student;
import com.ibb.yurtlar.entity.StudentDocument;
import com.ibb.yurtlar.enums.DocumentReviewDecision;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.enums.StudentDocumentStatus;
import com.ibb.yurtlar.exception.DocumentNotReadyForReviewException;
import com.ibb.yurtlar.exception.ReviewCommentRequiredException;
import com.ibb.yurtlar.exception.StudentDocumentNotFoundException;
import com.ibb.yurtlar.exception.UserIsNotReviewerException;
import com.ibb.yurtlar.exception.UserNotFoundException;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.DocumentReviewRepository;
import com.ibb.yurtlar.repository.StudentDocumentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocumentReviewService {

    private final DocumentReviewRepository
            documentReviewRepository;

    private final StudentDocumentRepository
            studentDocumentRepository;

    private final AppUserRepository
            appUserRepository;

    public DocumentReviewService(
            DocumentReviewRepository documentReviewRepository,
            StudentDocumentRepository studentDocumentRepository,
            AppUserRepository appUserRepository
    ) {
        this.documentReviewRepository =
                documentReviewRepository;

        this.studentDocumentRepository =
                studentDocumentRepository;

        this.appUserRepository =
                appUserRepository;
    }

    @Transactional
    public DocumentReviewResponse create(
            CreateDocumentReviewRequest request
    ) {
        StudentDocument document =
                studentDocumentRepository
                        .findById(
                                request.studentDocumentId()
                        )
                        .orElseThrow(
                                () -> new StudentDocumentNotFoundException(
                                        request.studentDocumentId()
                                )
                        );

        AppUser reviewer =
                validateReviewer(
                        request.reviewerUserId()
                );

        if (document.getStatus()
                != StudentDocumentStatus.UPLOADED) {

            throw new DocumentNotReadyForReviewException(
                    document.getId()
            );
        }

        String normalizedComment =
                normalizeComment(
                        request.comment()
                );

        validateComment(
                request.decision(),
                normalizedComment
        );

        DocumentReview review =
                new DocumentReview();

        review.setStudentDocument(document);
        review.setReviewer(reviewer);
        review.setDecision(
                request.decision()
        );
        review.setComment(
                normalizedComment
        );

        updateDocumentStatus(
                document,
                request.decision()
        );

        DocumentReview savedReview =
                documentReviewRepository.save(
                        review
                );

        return toResponse(
                savedReview
        );
    }

    @Transactional(readOnly = true)
    public List<DocumentReviewResponse> getByDocumentId(
            Long studentDocumentId
    ) {
        if (!studentDocumentRepository
                .existsById(studentDocumentId)) {

            throw new StudentDocumentNotFoundException(
                    studentDocumentId
            );
        }

        return documentReviewRepository
                .findAllByStudentDocument_IdOrderByReviewedAtDesc(
                        studentDocumentId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentReviewResponse> getByReviewerId(
            Long reviewerUserId
    ) {
        validateReviewer(
                reviewerUserId
        );

        return documentReviewRepository
                .findAllByReviewer_IdOrderByReviewedAtDesc(
                        reviewerUserId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentReviewResponse>
    getRecentReviewsByDormitory(
            Long dormitoryId
    ) {
        return documentReviewRepository
                .findRecentByDormitoryAndActiveTerm(
                        dormitoryId,
                        PageRequest.of(0, 10)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateComment(
            DocumentReviewDecision decision,
            String comment
    ) {
        boolean commentRequired =
                decision
                        == DocumentReviewDecision.REJECTED
                        || decision
                        == DocumentReviewDecision.REVISION_REQUIRED;

        if (commentRequired && comment == null) {
            throw new ReviewCommentRequiredException();
        }
    }

    private void updateDocumentStatus(
            StudentDocument document,
            DocumentReviewDecision decision
    ) {
        StudentDocumentStatus newStatus =
                switch (decision) {
                    case APPROVED ->
                            StudentDocumentStatus.APPROVED;

                    case REJECTED ->
                            StudentDocumentStatus.REJECTED;

                    case REVISION_REQUIRED ->
                            StudentDocumentStatus.REVISION_REQUIRED;
                };

        document.setStatus(
                newStatus
        );
    }

    private String normalizeComment(
            String comment
    ) {
        if (comment == null) {
            return null;
        }

        String trimmedComment =
                comment.trim();

        return trimmedComment.isEmpty()
                ? null
                : trimmedComment;
    }

    private DocumentReviewResponse toResponse(
            DocumentReview review
    ) {
        StudentDocument document =
                review.getStudentDocument();

        Admission admission =
                document.getAdmission();

        Student student =
                admission.getStudent();

        AppUser studentUser =
                student.getUser();

        AppUser reviewer =
                review.getReviewer();

        return new DocumentReviewResponse(
                review.getId(),

                document.getId(),
                document.getDocumentType().getName(),
                document.getOriginalFileName(),

                student.getId(),
                studentUser.getFirstName(),
                studentUser.getLastName(),

                reviewer.getId(),
                reviewer.getFirstName(),
                reviewer.getLastName(),

                review.getDecision(),
                review.getComment(),
                review.getReviewedAt()
        );
    }

    private AppUser validateReviewer(
            Long reviewerId
    ) {
        AppUser reviewer =
                appUserRepository
                        .findById(reviewerId)
                        .orElseThrow(
                                () -> new UserNotFoundException(
                                        reviewerId
                                )
                        );

        if (reviewer.getRole() != Role.REVIEWER) {
            throw new UserIsNotReviewerException(
                    reviewerId
            );
        }

        return reviewer;
    }
}