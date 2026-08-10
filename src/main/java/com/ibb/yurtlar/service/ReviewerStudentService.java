package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.ReviewerStudentResponse;
import com.ibb.yurtlar.dto.ReviewerStudentDocumentProcessItemResponse;
import com.ibb.yurtlar.dto.ReviewerStudentDocumentProcessResponse;
import com.ibb.yurtlar.entity.Admission;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.enums.StudentDocumentStatus;
import com.ibb.yurtlar.entity.StudentDocument;
import com.ibb.yurtlar.entity.TermDocumentRequirement;
import com.ibb.yurtlar.exception.AdmissionAccessDeniedException;
import com.ibb.yurtlar.exception.ActiveDormitoryTermNotFoundException;
import com.ibb.yurtlar.exception.InvalidCredentialsException;
import com.ibb.yurtlar.exception.InvalidUserConfigurationException;
import com.ibb.yurtlar.exception.UserIsNotReviewerException;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.AdmissionRepository;
import com.ibb.yurtlar.repository.DormitoryTermRepository;
import com.ibb.yurtlar.repository.StudentDocumentRepository;
import com.ibb.yurtlar.repository.StudentRepository;
import com.ibb.yurtlar.repository.TermDocumentRequirementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReviewerStudentService {

    private final AppUserRepository appUserRepository;
    private final DormitoryTermRepository dormitoryTermRepository;
    private final StudentRepository studentRepository;
    private final AdmissionRepository admissionRepository;
    private final StudentDocumentRepository studentDocumentRepository;
    private final TermDocumentRequirementRepository termDocumentRequirementRepository;

    public ReviewerStudentService(
            AppUserRepository appUserRepository,
            DormitoryTermRepository dormitoryTermRepository,
            StudentRepository studentRepository,
            AdmissionRepository admissionRepository,
            StudentDocumentRepository studentDocumentRepository,
            TermDocumentRequirementRepository termDocumentRequirementRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.dormitoryTermRepository = dormitoryTermRepository;
        this.studentRepository = studentRepository;
        this.admissionRepository = admissionRepository;
        this.studentDocumentRepository = studentDocumentRepository;
        this.termDocumentRequirementRepository = termDocumentRequirementRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewerStudentResponse> getMyDormitoryStudents(
            String reviewerEmail
    ) {
        AppUser reviewer = appUserRepository
                .findByNormalizedEmail(reviewerEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (reviewer.getRole() != Role.REVIEWER) {
            throw new UserIsNotReviewerException(reviewer.getId());
        }

        if (!reviewer.isActive()) {
            throw new InvalidCredentialsException();
        }

        Dormitory dormitory = reviewer.getDormitory();

        if (dormitory == null) {
            throw new InvalidUserConfigurationException(
                    "Reviewer kullanıcısına bir yurt atanmamıştır."
            );
        }

        DormitoryTerm activeTerm = dormitoryTermRepository
                .findByActiveTrue()
                .orElseThrow(ActiveDormitoryTermNotFoundException::new);

        return studentRepository.findReviewerStudentsByDormitoryAndTerm(
                dormitory.getId(),
                activeTerm.getId()
        );
    }

    @Transactional(readOnly = true)
    public ReviewerStudentDocumentProcessResponse getDocumentProcess(
            Long studentId,
            String reviewerEmail
    ) {
        AppUser reviewer = findActiveReviewer(reviewerEmail);
        Dormitory dormitory = requireReviewerDormitory(reviewer);
        DormitoryTerm activeTerm = findActiveTerm();

        Admission admission = admissionRepository
                .findApprovedReviewerStudentAdmission(
                        studentId,
                        dormitory.getId(),
                        activeTerm.getId()
                )
                .orElseThrow(() -> new AdmissionAccessDeniedException(
                        "Bu öğrencinin aktif belge sürecine erişim yetkiniz bulunmamaktadır."
                ));

        List<TermDocumentRequirement> requirements =
                termDocumentRequirementRepository
                        .findRequiredDocumentsByDormitoryTerm(activeTerm.getId());

        Map<Long, StudentDocument> documentsByType = studentDocumentRepository
                .findAllByAdmissionId(admission.getId())
                .stream()
                .collect(Collectors.toMap(
                        document -> document.getDocumentType().getId(),
                        Function.identity()
                ));

        List<ReviewerStudentDocumentProcessItemResponse> items = requirements
                .stream()
                .map(requirement -> {
                    Long documentTypeId = requirement.getDocumentType().getId();
                    StudentDocument document = documentsByType.get(documentTypeId);

                    return new ReviewerStudentDocumentProcessItemResponse(
                            documentTypeId,
                            requirement.getDocumentType().getName(),
                            document != null,
                            document == null ? null : document.getStatus()
                    );
                })
                .toList();

        int approvedCount = countStatus(items, StudentDocumentStatus.APPROVED);
        int uploadedCount = countStatus(items, StudentDocumentStatus.UPLOADED);
        int revisionCount = countStatus(items, StudentDocumentStatus.REVISION_REQUIRED);
        int rejectedCount = countStatus(items, StudentDocumentStatus.REJECTED);
        int missingCount = Math.toIntExact(items.stream().filter(item -> !item.uploaded()).count());
        boolean completed = !items.isEmpty() && approvedCount == items.size();
        AppUser studentUser = admission.getStudent().getUser();

        return new ReviewerStudentDocumentProcessResponse(
                studentId,
                studentUser.getFirstName(),
                studentUser.getLastName(),
                admission.getStudent().getIdentityNumber(),
                activeTerm.getId(),
                activeTerm.getName(),
                items.size(),
                approvedCount,
                missingCount,
                uploadedCount,
                revisionCount,
                rejectedCount,
                completed,
                items
        );
    }

    private int countStatus(
            List<ReviewerStudentDocumentProcessItemResponse> items,
            StudentDocumentStatus status
    ) {
        return Math.toIntExact(items.stream()
                .filter(item -> item.status() == status)
                .count());
    }

    private AppUser findActiveReviewer(String reviewerEmail) {
        AppUser reviewer = appUserRepository
                .findByNormalizedEmail(reviewerEmail)
                .orElseThrow(InvalidCredentialsException::new);

        if (reviewer.getRole() != Role.REVIEWER) {
            throw new UserIsNotReviewerException(reviewer.getId());
        }

        if (!reviewer.isActive()) {
            throw new InvalidCredentialsException();
        }

        return reviewer;
    }

    private Dormitory requireReviewerDormitory(AppUser reviewer) {
        Dormitory dormitory = reviewer.getDormitory();

        if (dormitory == null) {
            throw new InvalidUserConfigurationException(
                    "Reviewer kullanıcısına bir yurt atanmamıştır."
            );
        }

        return dormitory;
    }

    private DormitoryTerm findActiveTerm() {
        return dormitoryTermRepository
                .findByActiveTrue()
                .orElseThrow(ActiveDormitoryTermNotFoundException::new);
    }
}
