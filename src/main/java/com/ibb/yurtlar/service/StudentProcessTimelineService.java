package com.ibb.yurtlar.service;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.*;

import com.ibb.yurtlar.exception.BusinessException;

import com.ibb.yurtlar.dto.StudentProcessTimelineItemResponse;
import com.ibb.yurtlar.dto.StudentProcessTimelineResponse;
import com.ibb.yurtlar.entity.Admission;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.AuditLog;
import com.ibb.yurtlar.entity.DocumentReview;
import com.ibb.yurtlar.entity.Student;
import com.ibb.yurtlar.entity.StudentDocument;
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditEntityType;
import com.ibb.yurtlar.enums.DocumentReviewDecision;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.repository.AdmissionRepository;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.AuditLogRepository;
import com.ibb.yurtlar.repository.DocumentReviewRepository;
import com.ibb.yurtlar.repository.StudentDocumentRepository;
import com.ibb.yurtlar.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StudentProcessTimelineService {
    private static final List<AuditAction> ADMISSION_ACTIONS = List.of(
            AuditAction.ADMISSION_APPROVED,
            AuditAction.ADMISSION_REJECTED,
            AuditAction.DOCUMENT_PROCESS_COMPLETED
    );
    private static final List<AuditAction> DOCUMENT_ACTIONS = List.of(
            AuditAction.DOCUMENT_UPLOADED,
            AuditAction.DOCUMENT_REUPLOADED
    );

    private final AppUserRepository appUserRepository;
    private final StudentRepository studentRepository;
    private final AdmissionRepository admissionRepository;
    private final StudentDocumentRepository studentDocumentRepository;
    private final DocumentReviewRepository documentReviewRepository;
    private final AuditLogRepository auditLogRepository;

    public StudentProcessTimelineService(
            AppUserRepository appUserRepository,
            StudentRepository studentRepository,
            AdmissionRepository admissionRepository,
            StudentDocumentRepository studentDocumentRepository,
            DocumentReviewRepository documentReviewRepository,
            AuditLogRepository auditLogRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.studentRepository = studentRepository;
        this.admissionRepository = admissionRepository;
        this.studentDocumentRepository = studentDocumentRepository;
        this.documentReviewRepository = documentReviewRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public StudentProcessTimelineResponse getMyTimeline(String authenticatedEmail) {
        AppUser user = appUserRepository.findByNormalizedEmail(authenticatedEmail)
                .orElseThrow(() -> new BusinessException(INVALID_CREDENTIALS));
        if (user.getRole() != Role.STUDENT) {
            throw new BusinessException(USER_IS_NOT_STUDENT, user.getId());
        }
        if (!user.isActive()) {
            throw new BusinessException(INVALID_CREDENTIALS);
        }

        Student student = studentRepository.findByUserEmail(authenticatedEmail)
                .orElseThrow(() -> new BusinessException(STUDENT_NOT_FOUND, (Object) null));
        Admission admission = admissionRepository
                .findByStudent_IdAndDormitoryTerm_ActiveTrue(student.getId())
                .orElseThrow(() -> new BusinessException(ACTIVE_ADMISSION_NOT_FOUND_FOR_CURRENT_STUDENT));
        List<StudentDocument> documents = studentDocumentRepository
                .findAllByAdmissionId(admission.getId());
        Map<Long, StudentDocument> documentsById = documents.stream().collect(
                Collectors.toMap(StudentDocument::getId, Function.identity())
        );

        List<StudentProcessTimelineItemResponse> items = new ArrayList<>();
        auditLogRepository
                .findByEntityTypeAndEntityIdAndActionInOrderByCreatedAtAscIdAsc(
                        AuditEntityType.ADMISSION,
                        admission.getId(),
                        ADMISSION_ACTIONS
                )
                .stream().map(audit -> fromAudit(audit, documentsById)).forEach(items::add);

        List<Long> documentIds = documents.stream().map(StudentDocument::getId).toList();
        if (!documentIds.isEmpty()) {
            auditLogRepository
                    .findByEntityTypeAndEntityIdInAndActionInOrderByCreatedAtAscIdAsc(
                            AuditEntityType.STUDENT_DOCUMENT,
                            documentIds,
                            DOCUMENT_ACTIONS
                    )
                    .stream().map(audit -> fromAudit(audit, documentsById)).forEach(items::add);
        }

        documentReviewRepository.findAllForStudentTimelineByAdmissionId(admission.getId())
                .stream().map(this::fromReview).forEach(items::add);
        items.sort(Comparator
                .comparing(StudentProcessTimelineItemResponse::createdAt)
                .thenComparing(StudentProcessTimelineItemResponse::key));

        return new StudentProcessTimelineResponse(
                admission.getDormitoryTerm().getId(),
                admission.getDormitoryTerm().getName(),
                items
        );
    }

    private StudentProcessTimelineItemResponse fromAudit(
            AuditLog audit,
            Map<Long, StudentDocument> documentsById
    ) {
        StudentDocument document = audit.getEntityType() == AuditEntityType.STUDENT_DOCUMENT
                ? documentsById.get(audit.getEntityId()) : null;
        return new StudentProcessTimelineItemResponse(
                "audit-" + audit.getId(),
                audit.getAction(),
                document == null ? null : document.getDocumentType().getId(),
                document == null ? null : audit.getTargetLabel(),
                studentDescription(audit),
                audit.getCreatedAt(),
                null,
                null
        );
    }

    private StudentProcessTimelineItemResponse fromReview(DocumentReview review) {
        StudentDocument document = review.getStudentDocument();
        AuditAction action = switch (review.getDecision()) {
            case APPROVED -> AuditAction.DOCUMENT_APPROVED;
            case REJECTED -> AuditAction.DOCUMENT_REJECTED;
            case REVISION_REQUIRED -> AuditAction.DOCUMENT_REVISION_REQUIRED;
        };
        return new StudentProcessTimelineItemResponse(
                "review-" + review.getId(),
                action,
                document.getDocumentType().getId(),
                document.getDocumentType().getName(),
                reviewDescription(document.getDocumentType().getName(), review.getDecision()),
                review.getReviewedAt(),
                review.getDecision(),
                review.getComment()
        );
    }

    private String studentDescription(AuditLog audit) {
        return switch (audit.getAction()) {
            case ADMISSION_APPROVED -> "Kabulünüz onaylandı.";
            case ADMISSION_REJECTED -> "Kabulünüz reddedildi.";
            case DOCUMENT_UPLOADED -> audit.getTargetLabel() + " belgesini yüklediniz.";
            case DOCUMENT_REUPLOADED -> audit.getTargetLabel() + " belgesini yeniden yüklediniz.";
            case DOCUMENT_PROCESS_COMPLETED -> "Kesin kayıt için gerekli belge süreciniz tamamlandı.";
            default -> audit.getDescription();
        };
    }

    private String reviewDescription(String documentTypeName, DocumentReviewDecision decision) {
        return switch (decision) {
            case APPROVED -> documentTypeName + " belgeniz onaylandı.";
            case REJECTED -> documentTypeName + " belgeniz reddedildi.";
            case REVISION_REQUIRED -> documentTypeName + " belgeniz için düzeltme istendi.";
        };
    }
}
