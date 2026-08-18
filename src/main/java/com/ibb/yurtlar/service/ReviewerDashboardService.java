package com.ibb.yurtlar.service;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.*;

import com.ibb.yurtlar.exception.BusinessException;

import com.ibb.yurtlar.dto.DocumentReviewResponse;
import com.ibb.yurtlar.dto.DormitoryStudentProgressResponse;
import com.ibb.yurtlar.dto.PendingDocumentTypeCountResponse;
import com.ibb.yurtlar.dto.ReviewerDashboardResponse;
import com.ibb.yurtlar.dto.StudentDocumentResponse;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.enums.StudentDocumentStatus;
import com.ibb.yurtlar.mapper.StudentDocumentMapper;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.DormitoryTermRepository;
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

    private final DormitoryStudentProgressService
            dormitoryStudentProgressService;

    private final DormitoryTermRepository
            dormitoryTermRepository;

    public ReviewerDashboardService(
            AppUserRepository appUserRepository,
            StudentDocumentRepository studentDocumentRepository,
            DocumentReviewService documentReviewService,
            StudentDocumentMapper studentDocumentMapper,
            DormitoryStudentProgressService
                    dormitoryStudentProgressService,
            DormitoryTermRepository dormitoryTermRepository
    ) {
        this.appUserRepository =
                appUserRepository;

        this.studentDocumentRepository =
                studentDocumentRepository;

        this.documentReviewService =
                documentReviewService;

        this.studentDocumentMapper =
                studentDocumentMapper;

        this.dormitoryStudentProgressService =
                dormitoryStudentProgressService;

        this.dormitoryTermRepository =
                dormitoryTermRepository;
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

        DormitoryTerm activeTerm =
                dormitoryTermRepository
                        .findByActiveTrue()
                        .orElseThrow(
                                () -> new BusinessException(ACTIVE_DORMITORY_TERM_NOT_FOUND)
                        );

        Long dormitoryId =
                dormitory.getId();

        DormitoryStudentProgressResponse progress =
                dormitoryStudentProgressService
                        .calculateProgress(
                                dormitoryId,
                                activeTerm.getId()
                        );

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

        List<PendingDocumentTypeCountResponse>
                pendingDocumentsByType =
                studentDocumentRepository
                        .findPendingDocumentCountsByType(
                                dormitoryId,
                                StudentDocumentStatus.UPLOADED
                        );

        List<StudentDocumentResponse>
                oldestPendingDocuments =
                studentDocumentRepository
                        .findOldestByActiveTermAndDormitoryAndStatus(
                                dormitoryId,
                                StudentDocumentStatus.UPLOADED,
                                PageRequest.of(
                                        0,
                                        10
                                )
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

                activeTerm.getId(),
                activeTerm.getName(),

                progress.activeStudentCount(),
                progress.completedStudentCount(),
                progress.incompleteStudentCount(),
                progress.actionRequiredStudentCount(),
                progress.studentCompletionPercentage(),

                pendingDocumentCount,
                approvedDocumentCount,
                rejectedDocumentCount,
                revisionRequiredDocumentCount,

                pendingDocumentsByType,
                oldestPendingDocuments,
                progress.actionRequiredStudents(),
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
            throw new BusinessException(USER_IS_NOT_REVIEWER,
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
                                () -> new BusinessException(USER_NOT_FOUND,
                                        reviewerId
                                )
                        );

        if (reviewer.getRole() != Role.REVIEWER) {
            throw new BusinessException(USER_IS_NOT_REVIEWER,
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
            throw new BusinessException(INVALID_USER_CONFIGURATION,
                    "Reviewer kullanıcısına bir yurt atanmamıştır."
            );
        }

        return dormitory;
    }
}
