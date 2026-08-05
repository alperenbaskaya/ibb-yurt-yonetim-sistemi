package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.DocumentReviewResponse;
import com.ibb.yurtlar.dto.ReviewerDashboardResponse;
import com.ibb.yurtlar.dto.StudentDocumentResponse;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.enums.DocumentReviewDecision;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.exception.UserIsNotReviewerException;
import com.ibb.yurtlar.exception.UserNotFoundException;
import com.ibb.yurtlar.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewerDashboardService {

    private final AppUserRepository appUserRepository;
    private final StudentDocumentService
            studentDocumentService;
    private final DocumentReviewService
            documentReviewService;

    public ReviewerDashboardService(
            AppUserRepository appUserRepository,
            StudentDocumentService studentDocumentService,
            DocumentReviewService documentReviewService
    ) {
        this.appUserRepository = appUserRepository;
        this.studentDocumentService =
                studentDocumentService;
        this.documentReviewService =
                documentReviewService;
    }

    @Transactional(readOnly = true)
    public ReviewerDashboardResponse getDashboard(
            Long reviewerId
    ) {
        AppUser reviewer =
                findReviewerById(reviewerId);

        long pendingDocumentCount =
                studentDocumentService
                        .getPendingDocumentCount();

        long approvedReviewCount =
                documentReviewService
                        .countReviewsByDecision(
                                reviewerId,
                                DocumentReviewDecision.APPROVED
                        );

        long rejectedReviewCount =
                documentReviewService
                        .countReviewsByDecision(
                                reviewerId,
                                DocumentReviewDecision.REJECTED
                        );

        long revisionRequiredReviewCount =
                documentReviewService
                        .countReviewsByDecision(
                                reviewerId,
                                DocumentReviewDecision.REVISION_REQUIRED
                        );

        List<StudentDocumentResponse>
                oldestPendingDocuments =
                studentDocumentService
                        .getOldestPendingDocuments();

        List<DocumentReviewResponse> recentReviews =
                documentReviewService
                        .getRecentReviews(reviewerId);

        return new ReviewerDashboardResponse(
                reviewer.getId(),
                reviewer.getFirstName(),
                reviewer.getLastName(),
                reviewer.getEmail(),

                pendingDocumentCount,
                approvedReviewCount,
                rejectedReviewCount,
                revisionRequiredReviewCount,

                oldestPendingDocuments,
                recentReviews
        );
    }

    private AppUser findReviewerById(Long reviewerId) {
        AppUser reviewer = appUserRepository
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