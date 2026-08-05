package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.DocumentReviewResponse;
import com.ibb.yurtlar.dto.ReviewerDashboardResponse;
import com.ibb.yurtlar.dto.StudentDocumentResponse;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.enums.StudentDocumentStatus;
import com.ibb.yurtlar.exception.InvalidUserConfigurationException;
import com.ibb.yurtlar.exception.UserIsNotReviewerException;
import com.ibb.yurtlar.exception.UserNotFoundException;
import com.ibb.yurtlar.mapper.StudentDocumentMapper;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.StudentDocumentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewerDashboardService {

    private final AppUserRepository
            appUserRepository;

    private final StudentDocumentRepository
            studentDocumentRepository;

    private final DocumentReviewService
            documentReviewService;

    private final StudentDocumentMapper
            studentDocumentMapper;

    public ReviewerDashboardService(
            AppUserRepository appUserRepository,
            StudentDocumentRepository studentDocumentRepository,
            DocumentReviewService documentReviewService,
            StudentDocumentMapper studentDocumentMapper
    ) {
        this.appUserRepository =
                appUserRepository;

        this.studentDocumentRepository =
                studentDocumentRepository;

        this.documentReviewService =
                documentReviewService;

        this.studentDocumentMapper =
                studentDocumentMapper;
    }

    @Transactional(readOnly = true)
    public ReviewerDashboardResponse getDashboard(
            Long reviewerId
    ) {
        AppUser reviewer =
                findReviewerById(
                        reviewerId
                );

        Dormitory dormitory =
                getReviewerDormitory(
                        reviewer
                );

        Long dormitoryId =
                dormitory.getId();

        long pendingDocumentCount =
                countDocumentsByStatus(
                        dormitoryId,
                        StudentDocumentStatus.UPLOADED
                );

        long approvedDocumentCount =
                countDocumentsByStatus(
                        dormitoryId,
                        StudentDocumentStatus.APPROVED
                );

        long rejectedDocumentCount =
                countDocumentsByStatus(
                        dormitoryId,
                        StudentDocumentStatus.REJECTED
                );

        long revisionRequiredDocumentCount =
                countDocumentsByStatus(
                        dormitoryId,
                        StudentDocumentStatus.REVISION_REQUIRED
                );

        List<StudentDocumentResponse>
                oldestPendingDocuments =
                studentDocumentRepository
                        .findOldestByActiveTermAndDormitoryAndStatus(
                                dormitoryId,
                                StudentDocumentStatus.UPLOADED,
                                PageRequest.of(0, 10)
                        )
                        .stream()
                        .map(
                                studentDocumentMapper::toResponse
                        )
                        .toList();

        List<DocumentReviewResponse>
                recentDormitoryReviews =
                documentReviewService
                        .getRecentReviewsByDormitory(
                                dormitoryId
                        );

        return new ReviewerDashboardResponse(
                reviewer.getId(),
                reviewer.getFirstName(),
                reviewer.getLastName(),
                reviewer.getEmail(),

                dormitory.getId(),
                dormitory.getName(),

                pendingDocumentCount,
                approvedDocumentCount,
                rejectedDocumentCount,
                revisionRequiredDocumentCount,

                oldestPendingDocuments,
                recentDormitoryReviews
        );
    }

    @Transactional(readOnly = true)
    public ReviewerDashboardResponse getMyDashboard(
            String email
    ) {
        AppUser reviewer =
                appUserRepository
                        .findByNormalizedEmail(
                                email
                        )
                        .orElseThrow(
                                () -> new UsernameNotFoundException(
                                        "Giriş yapan kullanıcı bulunamadı."
                                )
                        );

        if (reviewer.getRole() != Role.REVIEWER) {
            throw new UserIsNotReviewerException(
                    reviewer.getId()
            );
        }

        return getDashboard(
                reviewer.getId()
        );
    }

    private long countDocumentsByStatus(
            Long dormitoryId,
            StudentDocumentStatus status
    ) {
        return studentDocumentRepository
                .countByActiveTermAndDormitoryAndStatus(
                        dormitoryId,
                        status
                );
    }

    private AppUser findReviewerById(
            Long reviewerId
    ) {
        AppUser reviewer =
                appUserRepository
                        .findById(
                                reviewerId
                        )
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

    private Dormitory getReviewerDormitory(
            AppUser reviewer
    ) {
        Dormitory dormitory =
                reviewer.getDormitory();

        if (dormitory == null) {
            throw new InvalidUserConfigurationException(
                    "Reviewer kullanıcısına bir yurt atanmamıştır."
            );
        }

        return dormitory;
    }
}