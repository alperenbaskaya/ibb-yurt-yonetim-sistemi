package com.ibb.yurtlar.service;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.*;

import com.ibb.yurtlar.exception.BusinessException;

import com.ibb.yurtlar.dto.AdminDocumentReviewTraceResponse;
import com.ibb.yurtlar.dto.AdminStudentDocumentInspectionItemResponse;
import com.ibb.yurtlar.dto.AdminStudentDocumentInspectionResponse;
import com.ibb.yurtlar.entity.Admission;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.DocumentReview;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.entity.StudentDocument;
import com.ibb.yurtlar.entity.TermDocumentRequirement;
import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.enums.StudentDocumentStatus;
import com.ibb.yurtlar.repository.AdmissionRepository;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.DocumentReviewRepository;
import com.ibb.yurtlar.repository.DormitoryTermRepository;
import com.ibb.yurtlar.repository.StudentDocumentRepository;
import com.ibb.yurtlar.repository.TermDocumentRequirementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AdminStudentDocumentInspectionService {

    private final AppUserRepository appUserRepository;
    private final DormitoryTermRepository dormitoryTermRepository;
    private final AdmissionRepository admissionRepository;
    private final TermDocumentRequirementRepository requirementRepository;
    private final StudentDocumentRepository studentDocumentRepository;
    private final DocumentReviewRepository documentReviewRepository;

    public AdminStudentDocumentInspectionService(
            AppUserRepository appUserRepository,
            DormitoryTermRepository dormitoryTermRepository,
            AdmissionRepository admissionRepository,
            TermDocumentRequirementRepository requirementRepository,
            StudentDocumentRepository studentDocumentRepository,
            DocumentReviewRepository documentReviewRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.dormitoryTermRepository = dormitoryTermRepository;
        this.admissionRepository = admissionRepository;
        this.requirementRepository = requirementRepository;
        this.studentDocumentRepository = studentDocumentRepository;
        this.documentReviewRepository = documentReviewRepository;
    }

    @Transactional(readOnly = true)
    public AdminStudentDocumentInspectionResponse getInspection(
            Long studentId,
            String adminEmail
    ) {
        AppUser admin = findActiveAdmin(adminEmail);
        Long adminDormitoryId = resolveDormitoryScope(admin);
        DormitoryTerm activeTerm = dormitoryTermRepository
                .findByActiveTrue()
                .orElseThrow(() -> new BusinessException(ACTIVE_DORMITORY_TERM_NOT_FOUND));

        Admission admission = admissionRepository
                .findAdminInspectionAdmission(
                        studentId,
                        activeTerm.getId(),
                        adminDormitoryId
                )
                .orElseThrow(() -> new BusinessException(STUDENT_MANAGEMENT_ACCESS_DENIED_MESSAGE, "Bu öğrencinin aktif dönem belge sürecini inceleme yetkiniz bulunmamaktadır."
                ));

        List<TermDocumentRequirement> requirements = requirementRepository
                .findRequiredDocumentsByDormitoryTerm(activeTerm.getId());
        Map<Long, StudentDocument> documentsByType = studentDocumentRepository
                .findAllByAdmissionId(admission.getId())
                .stream()
                .collect(Collectors.toMap(
                        document -> document.getDocumentType().getId(),
                        Function.identity()
                ));
        Map<Long, List<DocumentReview>> reviewsByDocumentId = documentReviewRepository
                .findAllForAdminInspectionByAdmissionId(admission.getId())
                .stream()
                .collect(Collectors.groupingBy(
                        review -> review.getStudentDocument().getId()
                ));

        List<AdminStudentDocumentInspectionItemResponse> items = requirements
                .stream()
                .map(requirement -> createItem(
                        requirement,
                        documentsByType,
                        reviewsByDocumentId
                ))
                .toList();

        int approvedCount = countStatus(items, StudentDocumentStatus.APPROVED);
        int uploadedCount = countStatus(items, StudentDocumentStatus.UPLOADED);
        int revisionCount = countStatus(items, StudentDocumentStatus.REVISION_REQUIRED);
        int rejectedCount = countStatus(items, StudentDocumentStatus.REJECTED);
        int missingCount = Math.toIntExact(items.stream()
                .filter(item -> !item.uploaded())
                .count());
        boolean completed = !items.isEmpty() && approvedCount == items.size();
        Dormitory dormitory = admission.getDormitory();
        AppUser studentUser = admission.getStudent().getUser();

        return new AdminStudentDocumentInspectionResponse(
                studentId,
                studentUser.getFirstName(),
                studentUser.getLastName(),
                admission.getStudent().getIdentityNumber(),
                dormitory.getId(),
                dormitory.getName(),
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

    private AdminStudentDocumentInspectionItemResponse createItem(
            TermDocumentRequirement requirement,
            Map<Long, StudentDocument> documentsByType,
            Map<Long, List<DocumentReview>> reviewsByDocumentId
    ) {
        Long documentTypeId = requirement.getDocumentType().getId();
        StudentDocument document = documentsByType.get(documentTypeId);

        if (document == null) {
            return new AdminStudentDocumentInspectionItemResponse(
                    documentTypeId,
                    requirement.getDocumentType().getName(),
                    true,
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of()
            );
        }

        List<AdminDocumentReviewTraceResponse> history = reviewsByDocumentId
                .getOrDefault(document.getId(), List.of())
                .stream()
                .map(this::toReviewTrace)
                .toList();

        return new AdminStudentDocumentInspectionItemResponse(
                documentTypeId,
                requirement.getDocumentType().getName(),
                true,
                true,
                document.getId(),
                document.getStatus(),
                document.getOriginalFileName(),
                document.getUploadedAt(),
                history.isEmpty() ? null : history.getFirst(),
                history
        );
    }

    private AdminDocumentReviewTraceResponse toReviewTrace(DocumentReview review) {
        AppUser reviewer = review.getReviewer();
        return new AdminDocumentReviewTraceResponse(
                reviewer.getId(),
                reviewer.getFirstName(),
                reviewer.getLastName(),
                review.getDecision(),
                review.getComment(),
                review.getReviewedAt()
        );
    }

    private int countStatus(
            List<AdminStudentDocumentInspectionItemResponse> items,
            StudentDocumentStatus status
    ) {
        return Math.toIntExact(items.stream()
                .filter(item -> item.status() == status)
                .count());
    }

    private AppUser findActiveAdmin(String email) {
        AppUser admin = appUserRepository
                .findByNormalizedEmail(email)
                .orElseThrow(() -> new BusinessException(INVALID_CREDENTIALS));

        if (admin.getRole() != Role.ADMIN) {
            throw new BusinessException(USER_IS_NOT_ADMIN, admin.getId());
        }
        if (!admin.isActive()) {
            throw new BusinessException(INVALID_CREDENTIALS);
        }
        return admin;
    }

    private Long resolveDormitoryScope(AppUser admin) {
        if (admin.getAdminScope() == AdminScope.GLOBAL) {
            return null;
        }
        if (admin.getAdminScope() != AdminScope.DORMITORY) {
            throw new BusinessException(INVALID_ADMIN_CONFIGURATION,
                    "Admin kullanıcısının yetki kapsamı geçersizdir."
            );
        }

        Dormitory dormitory = admin.getDormitory();
        if (dormitory == null) {
            throw new BusinessException(INVALID_ADMIN_CONFIGURATION,
                    "Yurt admini için yurt ataması zorunludur."
            );
        }
        return dormitory.getId();
    }
}
