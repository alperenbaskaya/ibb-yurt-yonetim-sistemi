package com.ibb.yurtlar.service;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.*;

import com.ibb.yurtlar.exception.BusinessException;

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
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.AuditEntityType;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.DocumentReviewRepository;
import com.ibb.yurtlar.repository.StudentDocumentRepository;
import com.ibb.yurtlar.repository.AdmissionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.exception.*;
import com.ibb.yurtlar.enums.DocumentReviewDecision;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.enums.StudentDocumentStatus;
import com.ibb.yurtlar.dto.DocumentCompletionResponse;
import com.ibb.yurtlar.enums.NotificationReferenceType;
import com.ibb.yurtlar.enums.NotificationType;

import java.util.List;

@Service
public class DocumentReviewService {

    private final DocumentReviewRepository
            documentReviewRepository;

    private final StudentDocumentRepository
            studentDocumentRepository;

    private final AppUserRepository
            appUserRepository;

    private final NotificationService
            notificationService;

    private final StudentDocumentService
            studentDocumentService;
    private final AdmissionRepository admissionRepository;
    private final AuditLogService auditLogService;

    public DocumentReviewService(
            DocumentReviewRepository documentReviewRepository,
            StudentDocumentRepository studentDocumentRepository,
            AppUserRepository appUserRepository,
            NotificationService notificationService,
            StudentDocumentService studentDocumentService,
            AdmissionRepository admissionRepository,
            AuditLogService auditLogService
    ) {
        this.documentReviewRepository =
                documentReviewRepository;

        this.studentDocumentRepository =
                studentDocumentRepository;

        this.appUserRepository =
                appUserRepository;

        this.notificationService =
                notificationService;

        this.studentDocumentService =
                studentDocumentService;
        this.admissionRepository = admissionRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public DocumentReviewResponse create(
            CreateDocumentReviewRequest request,
            String reviewerEmail
    ) {
        StudentDocument document =
                studentDocumentRepository
                        .findById(
                                request.studentDocumentId()
                        )
                        .orElseThrow(
                                () -> new BusinessException(STUDENT_DOCUMENT_NOT_FOUND,
                                        request.studentDocumentId()
                                )
                        );

        AppUser reviewer =
                validateReviewerByEmail(
                        reviewerEmail
                );

        studentDocumentService.validateReviewerDocumentAccess(
                reviewer,
                document
        );

        if (document.getStatus()
                != StudentDocumentStatus.UPLOADED) {

            throw new BusinessException(DOCUMENT_NOT_READY_FOR_REVIEW,
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

        createStudentDocumentReviewNotification(
                document,
                request.decision(),
                normalizedComment
        );

        recordReviewAudit(reviewerEmail, reviewer, document, request.decision());

        if (request.decision()
                == DocumentReviewDecision.APPROVED) {

            checkDocumentProcessCompletion(
                    document,
                    reviewerEmail
            );
        }

        return toResponse(
                savedReview
        );
    }

    @Transactional(readOnly = true)
    public List<DocumentReviewResponse>
    getByDocumentIdForReviewer(
            Long studentDocumentId,
            String reviewerEmail
    ) {
        StudentDocument document =
                studentDocumentRepository
                        .findById(studentDocumentId)
                        .orElseThrow(
                                () -> new BusinessException(STUDENT_DOCUMENT_NOT_FOUND,
                                        studentDocumentId
                                )
                        );

        AppUser reviewer =
                validateReviewerByEmail(
                        reviewerEmail
                );

        studentDocumentService.validateReviewerDocumentAccess(
                reviewer,
                document
        );

        return documentReviewRepository
                .findAllByStudentDocument_IdOrderByReviewedAtDesc(
                        studentDocumentId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentReviewResponse> getMyReviews(
            String reviewerEmail
    ) {
        AppUser reviewer =
                validateReviewerByEmail(
                        reviewerEmail
                );

        return documentReviewRepository
                .findAllByReviewer_IdOrderByReviewedAtDesc(
                        reviewer.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public com.ibb.yurtlar.dto.DocumentReviewPageResponse searchMyReviews(
            String reviewerEmail, String query, int page, int size
    ) {
        AppUser reviewer = validateReviewerByEmail(reviewerEmail);
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Invalid review history page request");
        }
        var result = documentReviewRepository.searchMyReviews(
                reviewer.getId(),
                query == null ? "" : query.trim().toLowerCase(java.util.Locale.ROOT),
                PageRequest.of(page, size)
        );
        return new com.ibb.yurtlar.dto.DocumentReviewPageResponse(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(),
                result.getTotalPages(), result.isFirst(), result.isLast()
        );
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
            throw new BusinessException(REVIEW_COMMENT_REQUIRED);
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

    private AppUser validateReviewerByEmail(
            String reviewerEmail
    ) {
        AppUser reviewer =
                appUserRepository
                        .findByNormalizedEmail(
                                reviewerEmail
                        )
                        .orElseThrow(
                                () -> new BusinessException(INVALID_CREDENTIALS)
                        );

        if (reviewer.getRole() != Role.REVIEWER) {
            throw new BusinessException(USER_IS_NOT_REVIEWER,
                    reviewer.getId()
            );
        }

        if (!reviewer.isActive()) {
            throw new BusinessException(INVALID_CREDENTIALS);
        }

        return reviewer;
    }

    private void createStudentDocumentReviewNotification(
            StudentDocument document,
            DocumentReviewDecision decision,
            String comment
    ) {
        AppUser studentUser =
                document
                        .getAdmission()
                        .getStudent()
                        .getUser();

        String documentTypeName =
                document
                        .getDocumentType()
                        .getName();

        NotificationType notificationType;
        String title;
        String message;

        switch (decision) {

            case APPROVED -> {
                notificationType =
                        NotificationType.DOCUMENT_APPROVED;

                title =
                        "Belgeniz Onaylandı";

                message =
                        documentTypeName
                                + " belgeniz onaylandı.";
            }

            case REJECTED -> {
                notificationType =
                        NotificationType.DOCUMENT_REJECTED;

                title =
                        "Belgeniz Reddedildi";

                message =
                        documentTypeName
                                + " belgeniz reddedildi.";

                if (comment != null) {
                    message +=
                            " Açıklama: "
                                    + comment;
                }
            }

            case REVISION_REQUIRED -> {
                notificationType =
                        NotificationType
                                .DOCUMENT_REVISION_REQUIRED;

                title =
                        "Belge Revizyonu Gerekli";

                message =
                        documentTypeName
                                + " belgeniz için revizyon istendi.";

                if (comment != null) {
                    message +=
                            " Açıklama: "
                                    + comment;
                }
            }

            default ->
                    throw new IllegalArgumentException(
                            "Desteklenmeyen değerlendirme kararı: "
                                    + decision
                    );
        }

            notificationService
                .createNotification(
                        studentUser.getId(),
                        notificationType,
                        title,
                        message,
                        NotificationReferenceType.STUDENT_DOCUMENT,
                        document.getId()
                );
    }

    void checkDocumentProcessCompletion(
            StudentDocument document,
            String reviewerEmail
    ) {
        Admission admission =
                document.getAdmission();

        // Completion belongs to one admission. Serializing on that row makes
        // the following completion check and both absent-check/insert pairs
        // atomic with respect to other reviews for the same process.
        Admission lockedAdmission = admissionRepository
                .findByIdForDocumentCompletionUpdate(admission.getId())
                .orElseThrow(() -> new BusinessException(ADMISSION_NOT_FOUND, admission.getId()));

        DocumentCompletionResponse completion =
                studentDocumentService
                        .getCompletionStatus(
                                lockedAdmission.getId()
                        );

        if (!completion.completed()) {
            return;
        }

        createStudentCompletionNotification(
                lockedAdmission
        );

        createDormitoryAdminCompletionNotifications(
                lockedAdmission
        );

        AppUser studentUser = lockedAdmission.getStudent().getUser();
        String studentName = studentUser.getFirstName() + " " + studentUser.getLastName();
        auditLogService.recordDocumentProcessCompletedIfAbsent(
                reviewerEmail,
                lockedAdmission,
                studentName + " öğrencisinin zorunlu belge süreci tamamlandı."
        );
    }

    private void recordReviewAudit(String reviewerEmail, AppUser reviewer,
                                   StudentDocument document,
                                   DocumentReviewDecision decision) {
        AuditAction action = switch (decision) {
            case APPROVED -> AuditAction.DOCUMENT_APPROVED;
            case REJECTED -> AuditAction.DOCUMENT_REJECTED;
            case REVISION_REQUIRED -> AuditAction.DOCUMENT_REVISION_REQUIRED;
        };
        String verb = switch (decision) {
            case APPROVED -> " onayladı.";
            case REJECTED -> " reddetti.";
            case REVISION_REQUIRED -> " için düzeltme istedi.";
        };
        Admission admission = document.getAdmission();
        AppUser studentUser = admission.getStudent().getUser();
        String reviewerName = reviewer.getFirstName() + " " + reviewer.getLastName();
        String studentName = studentUser.getFirstName() + " " + studentUser.getLastName();
        auditLogService.recordStudentEvent(
                reviewerEmail, AuditCategory.REVIEWER_ACTIVITY, action,
                AuditEntityType.STUDENT_DOCUMENT, document.getId(),
                document.getDocumentType().getName(), admission.getStudent(),
                admission.getDormitory(), reviewerName + ", " + studentName
                        + " öğrencisinin " + document.getDocumentType().getName() + verb
        );
    }

    private void createStudentCompletionNotification(
            Admission admission
    ) {
        AppUser studentUser =
                admission
                        .getStudent()
                        .getUser();

        notificationService
                .createNotificationIfAbsent(
                        studentUser.getId(),

                        NotificationType
                                .DOCUMENT_PROCESS_COMPLETED,

                        "Belge Süreciniz Tamamlandı",

                        "Tüm zorunlu belgeleriniz onaylandı. "
                                + "Yurt kayıt süreciniz tamamlandı "
                                + "ve yurda giriş yapabilirsiniz.",

                        NotificationReferenceType.ADMISSION,

                        admission.getId()
                );
    }

    private void createDormitoryAdminCompletionNotifications(
            Admission admission
    ) {
        Dormitory dormitory =
                admission.getDormitory();

        List<AppUser> dormitoryAdmins =
                appUserRepository
                        .findActiveDormitoryAdminsByDormitory(
                                dormitory.getId()
                        );

        AppUser studentUser =
                admission
                        .getStudent()
                        .getUser();

        String studentFullName =
                studentUser.getFirstName()
                        + " "
                        + studentUser.getLastName();

        for (AppUser dormitoryAdmin : dormitoryAdmins) {

            notificationService
                    .createNotificationIfAbsent(
                            dormitoryAdmin.getId(),

                            NotificationType
                                    .DOCUMENT_PROCESS_COMPLETED,

                            "Öğrenci Belge Sürecini Tamamladı",

                            studentFullName
                                    + " adlı öğrencinin tüm zorunlu "
                                    + "belgeleri onaylandı. "
                                    + "Öğrenci yurda giriş için hazır.",

                            NotificationReferenceType.ADMISSION,

                            admission.getId()
                    );
        }
    }
}
