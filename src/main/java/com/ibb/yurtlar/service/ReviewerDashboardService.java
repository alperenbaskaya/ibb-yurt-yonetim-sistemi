package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.DocumentReviewResponse;
import com.ibb.yurtlar.dto.PendingDocumentTypeCountResponse;
import com.ibb.yurtlar.dto.ReviewerActionRequiredStudentResponse;
import com.ibb.yurtlar.dto.ReviewerDashboardResponse;
import com.ibb.yurtlar.dto.StudentDocumentResponse;
import com.ibb.yurtlar.entity.Admission;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.entity.StudentDocument;
import com.ibb.yurtlar.entity.TermDocumentRequirement;
import com.ibb.yurtlar.enums.AdmissionStatus;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.enums.StudentDocumentStatus;
import com.ibb.yurtlar.exception.InvalidUserConfigurationException;
import com.ibb.yurtlar.exception.UserIsNotReviewerException;
import com.ibb.yurtlar.exception.UserNotFoundException;
import com.ibb.yurtlar.mapper.StudentDocumentMapper;
import com.ibb.yurtlar.repository.AdmissionRepository;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.StudentDocumentRepository;
import com.ibb.yurtlar.repository.TermDocumentRequirementRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReviewerDashboardService {

    private final AppUserRepository
            appUserRepository;

    private final AdmissionRepository
            admissionRepository;

    private final StudentDocumentRepository
            studentDocumentRepository;

    private final TermDocumentRequirementRepository
            termDocumentRequirementRepository;

    private final DocumentReviewService
            documentReviewService;

    private final StudentDocumentMapper
            studentDocumentMapper;

    public ReviewerDashboardService(
            AppUserRepository appUserRepository,
            AdmissionRepository admissionRepository,
            StudentDocumentRepository studentDocumentRepository,
            TermDocumentRequirementRepository
                    termDocumentRequirementRepository,
            DocumentReviewService documentReviewService,
            StudentDocumentMapper studentDocumentMapper
    ) {
        this.appUserRepository =
                appUserRepository;

        this.admissionRepository =
                admissionRepository;

        this.studentDocumentRepository =
                studentDocumentRepository;

        this.termDocumentRequirementRepository =
                termDocumentRequirementRepository;

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

        List<Admission> activeAdmissions =
                admissionRepository
                        .findActiveTermByDormitoryAndStatus(
                                dormitoryId,
                                AdmissionStatus.APPROVED
                        );

        List<StudentDocument> dormitoryDocuments =
                studentDocumentRepository
                        .findAllForActiveTermDormitoryStudents(
                                dormitoryId,
                                AdmissionStatus.APPROVED
                        );

        List<TermDocumentRequirement>
                requiredDocumentRequirements =
                findRequiredDocumentRequirements(
                        activeAdmissions
                );

        Map<Long, List<StudentDocument>>
                documentsByAdmissionId =
                dormitoryDocuments
                        .stream()
                        .collect(
                                Collectors.groupingBy(
                                        document ->
                                                document
                                                        .getAdmission()
                                                        .getId()
                                )
                        );

        List<ReviewerActionRequiredStudentResponse>
                allActionRequiredStudents =
                activeAdmissions
                        .stream()
                        .map(admission ->
                                createActionRequiredStudent(
                                        admission,
                                        requiredDocumentRequirements,
                                        documentsByAdmissionId
                                                .getOrDefault(
                                                        admission.getId(),
                                                        List.of()
                                                )
                                )
                        )
                        .filter(response ->
                                response.missingDocumentCount() > 0
                                        || response
                                        .revisionRequiredDocumentCount() > 0
                                        || response
                                        .rejectedDocumentCount() > 0
                        )
                        .toList();

        long activeStudentCount =
                activeAdmissions.size();

        long actionRequiredStudentCount =
                allActionRequiredStudents.size();

        long completedStudentCount =
                activeAdmissions
                        .stream()
                        .filter(admission ->
                                isStudentDocumentProcessCompleted(
                                        requiredDocumentRequirements,
                                        documentsByAdmissionId
                                                .getOrDefault(
                                                        admission.getId(),
                                                        List.of()
                                                )
                                )
                        )
                        .count();

        long incompleteStudentCount =
                activeStudentCount
                        - completedStudentCount;

        int studentCompletionPercentage =
                calculateStudentCompletionPercentage(
                        activeStudentCount,
                        completedStudentCount
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
                                PageRequest.of(0, 10)
                        )
                        .stream()
                        .map(
                                studentDocumentMapper::toResponse
                        )
                        .toList();

        List<ReviewerActionRequiredStudentResponse>
                actionRequiredStudents =
                allActionRequiredStudents
                        .stream()
                        .limit(10)
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

                activeStudentCount,
                completedStudentCount,
                incompleteStudentCount,
                actionRequiredStudentCount,
                studentCompletionPercentage,

                pendingDocumentCount,
                approvedDocumentCount,
                rejectedDocumentCount,
                revisionRequiredDocumentCount,

                pendingDocumentsByType,
                oldestPendingDocuments,
                actionRequiredStudents,
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

    private List<TermDocumentRequirement>
    findRequiredDocumentRequirements(
            List<Admission> activeAdmissions
    ) {
        if (activeAdmissions.isEmpty()) {
            return List.of();
        }

        Long activeTermId =
                activeAdmissions
                        .getFirst()
                        .getDormitoryTerm()
                        .getId();

        return termDocumentRequirementRepository
                .findRequiredDocumentsByDormitoryTerm(
                        activeTermId
                );
    }

    private boolean isStudentDocumentProcessCompleted(
            List<TermDocumentRequirement> requirements,
            List<StudentDocument> documents
    ) {
        if (requirements.isEmpty()) {
            return false;
        }

        Map<Long, StudentDocument>
                documentsByDocumentTypeId =
                documents
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        document ->
                                                document
                                                        .getDocumentType()
                                                        .getId(),
                                        Function.identity(),
                                        (first, second) -> second
                                )
                        );

        return requirements
                .stream()
                .allMatch(requirement -> {
                    StudentDocument document =
                            documentsByDocumentTypeId.get(
                                    requirement
                                            .getDocumentType()
                                            .getId()
                            );

                    return document != null
                            && document.getStatus()
                            == StudentDocumentStatus.APPROVED;
                });
    }

    private ReviewerActionRequiredStudentResponse
    createActionRequiredStudent(
            Admission admission,
            List<TermDocumentRequirement> requirements,
            List<StudentDocument> documents
    ) {
        Map<Long, StudentDocument>
                documentsByDocumentTypeId =
                documents
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        document ->
                                                document
                                                        .getDocumentType()
                                                        .getId(),
                                        Function.identity(),
                                        (first, second) -> second
                                )
                        );

        int missingDocumentCount =
                0;

        int revisionRequiredDocumentCount =
                0;

        int rejectedDocumentCount =
                0;

        for (TermDocumentRequirement requirement
                : requirements) {

            Long documentTypeId =
                    requirement
                            .getDocumentType()
                            .getId();

            StudentDocument document =
                    documentsByDocumentTypeId.get(
                            documentTypeId
                    );

            if (document == null) {
                missingDocumentCount++;
                continue;
            }

            if (document.getStatus()
                    == StudentDocumentStatus.REVISION_REQUIRED) {

                revisionRequiredDocumentCount++;
            }

            if (document.getStatus()
                    == StudentDocumentStatus.REJECTED) {

                rejectedDocumentCount++;
            }
        }

        AppUser studentUser =
                admission
                        .getStudent()
                        .getUser();

        return new ReviewerActionRequiredStudentResponse(
                admission.getStudent().getId(),
                admission.getId(),

                studentUser.getFirstName(),
                studentUser.getLastName(),

                missingDocumentCount,
                revisionRequiredDocumentCount,
                rejectedDocumentCount
        );
    }

    private int calculateStudentCompletionPercentage(
            long activeStudentCount,
            long completedStudentCount
    ) {
        if (activeStudentCount == 0) {
            return 0;
        }

        return (int) Math.round(
                completedStudentCount
                        * 100.0
                        / activeStudentCount
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