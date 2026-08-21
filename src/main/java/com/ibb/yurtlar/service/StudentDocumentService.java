package com.ibb.yurtlar.service;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.*;

import com.ibb.yurtlar.exception.BusinessException;

import com.ibb.yurtlar.dto.StoredFileInfo;
import com.ibb.yurtlar.dto.StudentDocumentResponse;
import com.ibb.yurtlar.entity.Admission;
import com.ibb.yurtlar.entity.DocumentType;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.entity.StudentDocument;
import com.ibb.yurtlar.entity.TermDocumentRequirement;
import com.ibb.yurtlar.enums.AdmissionStatus;
import com.ibb.yurtlar.enums.StudentDocumentStatus;
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.AuditEntityType;
import com.ibb.yurtlar.repository.AdmissionRepository;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.StudentDocumentRepository;
import com.ibb.yurtlar.repository.DormitoryTermRepository;
import com.ibb.yurtlar.repository.TermDocumentRequirementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ibb.yurtlar.dto.DownloadedFile;
import org.springframework.core.io.Resource;
import com.ibb.yurtlar.dto.StudentDocumentRequirementStatusResponse;
import com.ibb.yurtlar.dto.DocumentCompletionResponse;
import com.ibb.yurtlar.mapper.StudentDocumentMapper;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.enums.NotificationReferenceType;
import com.ibb.yurtlar.enums.NotificationType;
import com.ibb.yurtlar.kafka.event.DocumentUploadedEvent;
import java.util.UUID;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StudentDocumentService {

    private final StudentDocumentRepository studentDocumentRepository;

    private final AdmissionRepository admissionRepository;

    private final TermDocumentRequirementRepository termDocumentRequirementRepository;

    private final FileStorageService fileStorageService;

    private final TransactionalFileLifecycleService
            transactionalFileLifecycleService;

    private final StudentDocumentMapper studentDocumentMapper;

    private final AppUserRepository appUserRepository;

    private final DormitoryTermRepository dormitoryTermRepository;

    private final NotificationService notificationService;

    private final AuditLogService auditLogService;

    private final OutboxEventService outboxEventService;

    public StudentDocumentService(
            StudentDocumentRepository studentDocumentRepository,
            AdmissionRepository admissionRepository,
            TermDocumentRequirementRepository termDocumentRequirementRepository,
            FileStorageService fileStorageService,
            TransactionalFileLifecycleService transactionalFileLifecycleService,
            StudentDocumentMapper studentDocumentMapper,
            AppUserRepository appUserRepository,
            DormitoryTermRepository dormitoryTermRepository,
            NotificationService notificationService,
            AuditLogService auditLogService,
            OutboxEventService outboxEventService
    ) {
        this.studentDocumentRepository =
                studentDocumentRepository;

        this.admissionRepository =
                admissionRepository;

        this.termDocumentRequirementRepository =
                termDocumentRequirementRepository;

        this.fileStorageService =
                fileStorageService;

        this.transactionalFileLifecycleService =
                transactionalFileLifecycleService;

        this.studentDocumentMapper =
                studentDocumentMapper;

        this.appUserRepository = appUserRepository;

        this.dormitoryTermRepository = dormitoryTermRepository;

        this.notificationService = notificationService;
        this.auditLogService = auditLogService;

        this.outboxEventService = outboxEventService;
    }

    @Transactional
    public StudentDocumentResponse uploadMyDocument(
            String studentEmail,
            Long documentTypeId,
            MultipartFile file
    ) {
        Admission admission =
                findActiveAdmissionByStudentEmail(
                        studentEmail
                );

        validateAdmissionForUpload(
                admission
        );

        DormitoryTerm term =
                admission.getDormitoryTerm();

        TermDocumentRequirement requirement =
                termDocumentRequirementRepository
                        .findByDormitoryTerm_IdAndDocumentType_Id(
                                term.getId(),
                                documentTypeId
                        )
                        .orElseThrow(
                                () -> new BusinessException(DOCUMENT_NOT_REQUIRED_FOR_TERM,
                                        term.getId(),
                                        documentTypeId
                                )
                        );

        DocumentType documentType =
                requirement.getDocumentType();

        if (!documentType.isActive()) {
            throw new BusinessException(INACTIVE_DOCUMENT_TYPE,
                    documentType.getId()
            );
        }

        Optional<StudentDocument> existingDocument =
                studentDocumentRepository
                        .findByAdmission_IdAndDocumentType_Id(
                                admission.getId(),
                                documentType.getId()
                        );

        existingDocument.ifPresent(
                this::validateReplacementAllowed
        );

        StoredFileInfo storedFileInfo =
                fileStorageService.storeStudentDocument(
                        file,
                        admission.getId(),
                        documentType.getId()
                );

        String obsoleteFilePath = existingDocument
                .map(StudentDocument::getFilePath)
                .orElse(null);

        transactionalFileLifecycleService.registerUpload(
                storedFileInfo.relativePath(),
                obsoleteFilePath
        );

        if (existingDocument.isPresent()) {
            StudentDocumentResponse response = replaceExistingDocument(
                    existingDocument.get(),
                    storedFileInfo
            );
            recordUploadAudit(studentEmail, existingDocument.get(), AuditAction.DOCUMENT_REUPLOADED);
            return response;
        }

        StudentDocumentResponse response = createNewDocument(
                admission,
                documentType,
                storedFileInfo
        );
        StudentDocument savedDocument = studentDocumentRepository
                .findByAdmission_IdAndDocumentType_Id(admission.getId(), documentType.getId())
                .orElseThrow();
        recordUploadAudit(studentEmail, savedDocument, AuditAction.DOCUMENT_UPLOADED);
        return response;
    }

    private void recordUploadAudit(String studentEmail, StudentDocument document,
                                   AuditAction action) {
        Admission admission = document.getAdmission();
        AppUser studentUser = admission.getStudent().getUser();
        String studentName = studentUser.getFirstName() + " " + studentUser.getLastName();
        auditLogService.recordStudentEvent(
                studentEmail, AuditCategory.STUDENT_ACTIVITY, action,
                AuditEntityType.STUDENT_DOCUMENT, document.getId(),
                document.getDocumentType().getName(), admission.getStudent(),
                admission.getDormitory(), studentName + ", "
                        + document.getDocumentType().getName()
                        + (action == AuditAction.DOCUMENT_UPLOADED
                        ? " belgesini yükledi." : " belgesini yeniden yükledi.")
        );
    }

    @Transactional(readOnly = true)
    public List<StudentDocumentResponse> getByAdmissionId(
            Long admissionId
    ) {
        if (!admissionRepository.existsById(admissionId)) {
            throw new BusinessException(ADMISSION_NOT_FOUND, admissionId);
        }

        return studentDocumentRepository
                .findAllByAdmissionId(
                        admissionId
                )
                .stream()
                .map(studentDocumentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentDocumentResponse getByIdForAuthenticatedUser(
            Long documentId,
            String authenticatedEmail
    ) {
        StudentDocument document =
                findDocumentById(
                        documentId
                );

        AppUser authenticatedUser =
                findAuthenticatedUser(
                        authenticatedEmail
                );

        validateDocumentAccess(
                authenticatedUser,
                document
        );

        return studentDocumentMapper
                .toResponse(
                        document
                );
    }

    private void validateAdmissionForUpload(
            Admission admission
    ) {
        if (admission.getStatus()
                != AdmissionStatus.APPROVED) {

            throw new BusinessException(ADMISSION_NOT_APPROVED,
                    admission.getId()
            );
        }

        DormitoryTerm term =
                admission.getDormitoryTerm();

        if (!term.isActive()) {
            throw new BusinessException(INACTIVE_DORMITORY_TERM,
                    term.getId()
            );
        }

        LocalDate today = LocalDate.now();

        boolean beforeUploadStart =
                today.isBefore(
                        term.getDocumentUploadStartDate()
                );

        boolean afterUploadEnd =
                today.isAfter(
                        term.getDocumentUploadEndDate()
                );

        if (beforeUploadStart || afterUploadEnd) {
            throw new BusinessException(DOCUMENT_UPLOAD_CLOSED);
        }
    }

    private StudentDocumentResponse createNewDocument(
            Admission admission,
            DocumentType documentType,
            StoredFileInfo storedFileInfo
    ) {
        StudentDocument document = new StudentDocument();

        document.setAdmission(admission);

        document.setDocumentType(documentType);

        applyStoredFileInfo(document, storedFileInfo);

        document.setStatus(StudentDocumentStatus.UPLOADED);

        StudentDocument savedDocument = studentDocumentRepository.save(document);
        recordDocumentUploadedOutbox(savedDocument);

        createReviewerUploadNotification(savedDocument);

        return studentDocumentMapper.toResponse(savedDocument);
    }

    private StudentDocumentResponse replaceExistingDocument(
            StudentDocument document,
            StoredFileInfo storedFileInfo
    ) {
        StudentDocumentStatus previousStatus =
                document.getStatus();

        applyStoredFileInfo(
                document,
                storedFileInfo
        );

        document.setStatus(
                StudentDocumentStatus.UPLOADED
        );

        document.setUploadedAt(
                LocalDateTime.now()
        );

        studentDocumentRepository
                .saveAndFlush(
                        document
                );

        if (previousStatus
                == StudentDocumentStatus.REVISION_REQUIRED) {

            createReviewerReUploadNotification(
                    document
            );
        }

        return studentDocumentMapper
                .toResponse(
                        document
                );
    }

    private void validateReplacementAllowed(
            StudentDocument document
    ) {
        if (document.getStatus()
                != StudentDocumentStatus.REVISION_REQUIRED) {

            throw new BusinessException(DOCUMENT_REPLACEMENT_NOT_ALLOWED,
                    document.getId(),
                    document.getStatus()
            );
        }
    }

    private void applyStoredFileInfo(
            StudentDocument document,
            StoredFileInfo storedFileInfo
    ) {
        document.setOriginalFileName(
                storedFileInfo.originalFileName()
        );

        document.setStoredFileName(
                storedFileInfo.storedFileName()
        );

        document.setFilePath(
                storedFileInfo.relativePath()
        );

        document.setContentType(
                storedFileInfo.contentType()
        );

        document.setFileSize(
                storedFileInfo.fileSize()
        );
    }

    private StudentDocument findDocumentById(Long id) {
        return studentDocumentRepository
                .findById(id)
                .orElseThrow(
                        () -> new BusinessException(STUDENT_DOCUMENT_NOT_FOUND,
                                id
                        )
                );
    }

    @Transactional(readOnly = true)
    public DownloadedFile downloadForAuthenticatedUser(
            Long documentId,
            String authenticatedEmail
    ) {
        StudentDocument document =
                findDocumentById(
                        documentId
                );

        AppUser authenticatedUser =
                findAuthenticatedUser(
                        authenticatedEmail
                );

        validateDocumentAccess(
                authenticatedUser,
                document
        );

        Resource resource =
                fileStorageService.loadAsResource(
                        document.getFilePath()
                );

        return new DownloadedFile(
                resource,
                document.getOriginalFileName(),
                document.getContentType()
        );
    }

    @Transactional(readOnly = true)
    public List<StudentDocumentResponse>
    getPendingDocumentsForReviewer(
            String reviewerEmail
    ) {
        AppUser reviewer =
                findAuthenticatedUser(
                        reviewerEmail
                );

        if (reviewer.getRole() != Role.REVIEWER) {
            throw new BusinessException(USER_IS_NOT_REVIEWER,
                    reviewer.getId()
            );
        }

        Dormitory reviewerDormitory =
                reviewer.getDormitory();

        if (reviewerDormitory == null) {
            throw new BusinessException(INVALID_USER_CONFIGURATION,
                    "Reviewer kullanıcısına bir yurt atanmamıştır."
            );
        }

        return studentDocumentRepository
                .findAllByActiveTermAndDormitoryAndStatus(
                        reviewerDormitory.getId(),
                        StudentDocumentStatus.UPLOADED
                )
                .stream()
                .map(studentDocumentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudentDocumentRequirementStatusResponse>
    getRequirementStatusesByAdmissionId(Long admissionId) {

        Admission admission =
                findAdmissionById(
                        admissionId
                );

        Long dormitoryTermId =
                admission.getDormitoryTerm().getId();

        List<TermDocumentRequirement> requirements =
                termDocumentRequirementRepository
                        .findAllByDormitoryTerm_IdAndDocumentType_ActiveTrueOrderByDocumentType_NameAsc(
                                dormitoryTermId
                        );

        List<StudentDocument> uploadedDocuments =
                studentDocumentRepository
                        .findAllByAdmissionId(
                                admissionId
                        );

        return requirements.stream()
                .map(requirement ->
                        toRequirementStatusResponse(
                                requirement,
                                uploadedDocuments
                        )
                )
                .toList();
    }

    private StudentDocumentRequirementStatusResponse
    toRequirementStatusResponse(
            TermDocumentRequirement requirement,
            List<StudentDocument> uploadedDocuments
    ) {
        DocumentType documentType =
                requirement.getDocumentType();

        Optional<StudentDocument> matchingDocument =
                uploadedDocuments.stream()
                        .filter(document ->
                                document.getDocumentType()
                                        .getId()
                                        .equals(documentType.getId())
                        )
                        .findFirst();

        if (matchingDocument.isEmpty()) {
            return new StudentDocumentRequirementStatusResponse(
                    requirement.getId(),

                    documentType.getId(),
                    documentType.getName(),
                    documentType.getDescription(),

                    requirement.isRequired(),

                    false,

                    null,
                    null,
                    null,
                    null
            );
        }

        StudentDocument document =
                matchingDocument.get();

        return new StudentDocumentRequirementStatusResponse(
                requirement.getId(),

                documentType.getId(),
                documentType.getName(),
                documentType.getDescription(),

                requirement.isRequired(),

                true,

                document.getId(),
                document.getOriginalFileName(),
                document.getStatus(),
                document.getUploadedAt()
        );
    }

    @Transactional(readOnly = true)
    public DocumentCompletionResponse getCompletionStatus(
            Long admissionId
    ) {
        List<StudentDocumentRequirementStatusResponse> statuses =
                getRequirementStatusesByAdmissionId(admissionId);

        List<StudentDocumentRequirementStatusResponse>
                requiredDocuments = statuses.stream()
                .filter(
                        StudentDocumentRequirementStatusResponse
                                ::required
                )
                .toList();

        long approvedCount = requiredDocuments.stream()
                .filter(status ->
                        status.uploaded()
                                && status.status()
                                == StudentDocumentStatus.APPROVED
                )
                .count();

        boolean completed =
                !requiredDocuments.isEmpty()
                        && approvedCount
                        == requiredDocuments.size();

        return new DocumentCompletionResponse(
                admissionId,
                requiredDocuments.size(),
                Math.toIntExact(approvedCount),
                completed
        );
    }

    @Transactional(readOnly = true)
    public List<StudentDocumentResponse>
    getOldestPendingDocuments() {

        return studentDocumentRepository
                .findTop10ByStatusOrderByUploadedAtAsc(
                        StudentDocumentStatus.UPLOADED
                )
                .stream()
                .map(studentDocumentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getPendingDocumentCount() {
        return studentDocumentRepository
                .countByStatus(
                        StudentDocumentStatus.UPLOADED
                );
    }

    @Transactional(readOnly = true)
    public List<StudentDocumentResponse> getMyDocuments(
            String studentEmail
    ) {
        Admission admission =
                findActiveAdmissionByStudentEmail(
                        studentEmail
                );

        return studentDocumentRepository
                .findAllByAdmissionId(
                        admission.getId()
                )
                .stream()
                .map(studentDocumentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudentDocumentRequirementStatusResponse>
    getMyRequirementStatuses(
            String studentEmail
    ) {
        Admission admission =
                findActiveAdmissionByStudentEmail(
                        studentEmail
                );

        return getRequirementStatusesByAdmissionId(
                admission.getId()
        );
    }

    @Transactional(readOnly = true)
    public DocumentCompletionResponse getMyCompletionStatus(
            String studentEmail
    ) {
        Admission admission =
                findActiveAdmissionByStudentEmail(
                        studentEmail
                );

        return getCompletionStatus(
                admission.getId()
        );
    }

    private Admission findActiveAdmissionByStudentEmail(
            String studentEmail
    ) {
        return admissionRepository
                .findActiveAdmissionByStudentEmail(
                        studentEmail
                )
                .orElseThrow(
                        () -> new BusinessException(ACTIVE_ADMISSION_NOT_FOUND_FOR_CURRENT_STUDENT)
                );
    }

    private AppUser findAuthenticatedUser(
            String email
    ) {
        return appUserRepository
                .findByNormalizedEmail(
                        email
                )
                .orElseThrow(
                        () -> new BusinessException(INVALID_CREDENTIALS)
                );
    }

    private void validateDocumentAccess(
            AppUser authenticatedUser,
            StudentDocument document
    ) {
        switch (authenticatedUser.getRole()) {

            case STUDENT ->
                    validateStudentDocumentOwnership(
                            authenticatedUser,
                            document
                    );

            case REVIEWER ->
                    validateReviewerDocumentAccess(
                            authenticatedUser,
                            document
                    );

            case ADMIN ->
                    validateAdminDocumentAccess(
                            authenticatedUser,
                            document
                    );
        }
    }

    private void validateStudentDocumentOwnership(
            AppUser authenticatedUser,
            StudentDocument document
    ) {
        AppUser documentOwner =
                document
                        .getAdmission()
                        .getStudent()
                        .getUser();

        boolean ownsDocument =
                authenticatedUser
                        .getId()
                        .equals(
                                documentOwner.getId()
                        );

        if (!ownsDocument) {
            throw new BusinessException(STUDENT_DOCUMENT_ACCESS_DENIED,
                    document.getId()
            );
        }
    }

    private void validateDormitoryAccess(
            AppUser authenticatedUser,
            StudentDocument document
    ) {
        Dormitory userDormitory =
                authenticatedUser.getDormitory();

        Dormitory documentDormitory =
                document
                        .getAdmission()
                        .getDormitory();

        boolean sameDormitory =
                userDormitory != null
                        && documentDormitory != null
                        && userDormitory
                        .getId()
                        .equals(
                                documentDormitory.getId()
                        );

        if (!sameDormitory) {
            throw new BusinessException(STUDENT_DOCUMENT_ACCESS_DENIED,
                    document.getId()
            );
        }
    }

    void validateReviewerDocumentAccess(
            AppUser reviewer,
            StudentDocument document
    ) {
        if (reviewer.getRole() != Role.REVIEWER || !reviewer.isActive()) {
            throw new BusinessException(STUDENT_DOCUMENT_ACCESS_DENIED, document.getId());
        }

        Dormitory reviewerDormitory = reviewer.getDormitory();

        if (reviewerDormitory == null) {
            throw new BusinessException(STUDENT_DOCUMENT_ACCESS_DENIED, document.getId());
        }

        DormitoryTerm activeTerm = dormitoryTermRepository
                .findByActiveTrue()
                .orElseThrow(() -> new BusinessException(ACTIVE_DORMITORY_TERM_NOT_FOUND));

        boolean withinReviewerScope = studentDocumentRepository
                .isWithinReviewerDocumentScope(
                        document.getId(),
                        reviewerDormitory.getId(),
                        activeTerm.getId()
                );

        if (!withinReviewerScope) {
            throw new BusinessException(STUDENT_DOCUMENT_ACCESS_DENIED, document.getId());
        }
    }

    private void validateAdminDocumentAccess(
            AppUser admin,
            StudentDocument document
    ) {
        if (admin.getAdminScope()
                == AdminScope.GLOBAL) {

            return;
        }

        if (admin.getAdminScope()
                == AdminScope.DORMITORY) {

            validateDormitoryAccess(
                    admin,
                    document
            );

            return;
        }

        throw new BusinessException(INVALID_ADMIN_CONFIGURATION,
                "Admin kullanıcısının yetki kapsamı geçersizdir."
        );
    }

    @Transactional(readOnly = true)
    public List<StudentDocumentResponse>
    getByAdmissionIdForAuthenticatedUser(
            Long admissionId,
            String authenticatedEmail
    ) {
        Admission admission =
                findAdmissionById(
                        admissionId
                );

        AppUser authenticatedUser =
                findAuthenticatedUser(
                        authenticatedEmail
                );

        validateAdmissionAccess(
                authenticatedUser,
                admission
        );

        return studentDocumentRepository
                .findAllByAdmissionId(
                        admissionId
                )
                .stream()
                .map(studentDocumentMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudentDocumentRequirementStatusResponse>
    getRequirementStatusesForAuthenticatedUser(
            Long admissionId,
            String authenticatedEmail
    ) {
        Admission admission =
                findAdmissionById(
                        admissionId
                );

        AppUser authenticatedUser =
                findAuthenticatedUser(
                        authenticatedEmail
                );

        validateAdmissionAccess(
                authenticatedUser,
                admission
        );

        return getRequirementStatusesByAdmissionId(
                admissionId
        );
    }

    @Transactional(readOnly = true)
    public DocumentCompletionResponse
    getCompletionStatusForAuthenticatedUser(
            Long admissionId,
            String authenticatedEmail
    ) {
        Admission admission =
                findAdmissionById(
                        admissionId
                );

        AppUser authenticatedUser =
                findAuthenticatedUser(
                        authenticatedEmail
                );

        validateAdmissionAccess(
                authenticatedUser,
                admission
        );

        return getCompletionStatus(
                admissionId
        );
    }

    private Admission findAdmissionById(
            Long admissionId
    ) {
        return admissionRepository
                .findById(
                        admissionId
                )
                .orElseThrow(
                        () -> new BusinessException(ADMISSION_NOT_FOUND,
                                admissionId
                        )
                );
    }

    private void validateAdmissionAccess(
            AppUser authenticatedUser,
            Admission admission
    ) {
        switch (authenticatedUser.getRole()) {

            case REVIEWER ->
                    validateReviewerAdmissionAccess(
                            authenticatedUser,
                            admission
                    );

            case ADMIN ->
                    validateAdminAdmissionAccess(
                            authenticatedUser,
                            admission
                    );

            case STUDENT ->
                    throw new BusinessException(STUDENT_DOCUMENT_ACCESS_DENIED,
                            admission.getId()
                    );
        }
    }

    private void validateUserDormitoryMatchesAdmission(
            AppUser authenticatedUser,
            Admission admission
    ) {
        Dormitory userDormitory =
                authenticatedUser.getDormitory();

        Dormitory admissionDormitory =
                admission.getDormitory();

        boolean sameDormitory =
                userDormitory != null
                        && admissionDormitory != null
                        && userDormitory
                        .getId()
                        .equals(
                                admissionDormitory.getId()
                        );

        if (!sameDormitory) {
            throw new BusinessException(ADMISSION_ACCESS_DENIED,
                    admission.getId()
            );
        }
    }

    private void validateReviewerAdmissionAccess(
            AppUser reviewer,
            Admission admission
    ) {
        if (reviewer.getRole() != Role.REVIEWER || !reviewer.isActive()) {
            throw new BusinessException(ADMISSION_ACCESS_DENIED, admission.getId());
        }

        DormitoryTerm activeTerm = dormitoryTermRepository
                .findByActiveTrue()
                .orElseThrow(() -> new BusinessException(ACTIVE_DORMITORY_TERM_NOT_FOUND));

        Dormitory reviewerDormitory = reviewer.getDormitory();
        Dormitory admissionDormitory = admission.getDormitory();

        boolean withinReviewerScope = reviewerDormitory != null
                && admissionDormitory != null
                && reviewerDormitory.getId().equals(admissionDormitory.getId())
                && admission.getDormitoryTerm() != null
                && activeTerm.getId().equals(admission.getDormitoryTerm().getId())
                && admission.getStatus() == AdmissionStatus.APPROVED;

        if (!withinReviewerScope) {
            throw new BusinessException(ADMISSION_ACCESS_DENIED, admission.getId());
        }
    }

    private void validateAdminAdmissionAccess(
            AppUser admin,
            Admission admission
    ) {
        if (admin.getAdminScope()
                == AdminScope.GLOBAL) {

            return;
        }

        if (admin.getAdminScope()
                == AdminScope.DORMITORY) {

            validateUserDormitoryMatchesAdmission(
                    admin,
                    admission
            );

            return;
        }

        throw new BusinessException(INVALID_ADMIN_CONFIGURATION,
                "Admin kullanıcısının yetki kapsamı geçersizdir."
        );
    }

    private void createReviewerUploadNotification(
            StudentDocument document
    ) {
        Dormitory dormitory =
                document
                        .getAdmission()
                        .getDormitory();

        List<AppUser> reviewers =
                appUserRepository
                        .findActiveReviewersByDormitory(
                                dormitory.getId()
                        );

        AppUser studentUser =
                document
                        .getAdmission()
                        .getStudent()
                        .getUser();

        String studentName =
                studentUser.getFirstName()
                        + " "
                        + studentUser.getLastName();

        String documentName =
                document
                        .getDocumentType()
                        .getName();

        for (AppUser reviewer : reviewers) {

            notificationService
                    .createNotification(
                            reviewer.getId(),

                            NotificationType
                                    .DOCUMENT_UPLOADED,

                            "Yeni Belge Yüklendi",

                            studentName
                                    + ", "
                                    + documentName
                                    + " belgesini yükledi.",

                            NotificationReferenceType
                                    .STUDENT_DOCUMENT,

                            document.getId()
                    );
        }
    }

    private void createReviewerReUploadNotification(
            StudentDocument document
    ) {
        Dormitory dormitory =
                document
                        .getAdmission()
                        .getDormitory();

        List<AppUser> reviewers =
                appUserRepository
                        .findActiveReviewersByDormitory(
                                dormitory.getId()
                        );

        AppUser studentUser =
                document
                        .getAdmission()
                        .getStudent()
                        .getUser();

        String studentName =
                studentUser.getFirstName()
                        + " "
                        + studentUser.getLastName();

        String documentName =
                document
                        .getDocumentType()
                        .getName();

        for (AppUser reviewer : reviewers) {

            notificationService
                    .createNotification(
                            reviewer.getId(),

                            NotificationType
                                    .DOCUMENT_REUPLOADED,

                            "Belge Yeniden Yüklendi",

                            studentName
                                    + ", revizyon istenen "
                                    + documentName
                                    + " belgesini yeniden yükledi.",

                            NotificationReferenceType
                                    .STUDENT_DOCUMENT,

                            document.getId()
                    );
        }
    }

    private void recordDocumentUploadedOutbox(
            StudentDocument document
    ) {

        Admission admission =
                document.getAdmission();

        Long studentId =
                admission.getStudent().getId();

        String eventId =
                UUID.randomUUID().toString();

        DocumentUploadedEvent event =
                new DocumentUploadedEvent(
                        eventId,
                        "DOCUMENT_UPLOADED",
                        studentId,
                        admission.getId(),
                        document.getId(),
                        document.getDocumentType().getId(),
                        admission.getDormitory().getId(),
                        LocalDateTime.now()
                );

        outboxEventService.recordPending(
                eventId,
                "DOCUMENT_UPLOADED",
                "STUDENT_DOCUMENT",
                document.getId(),
                "dormitory-activity-events",
                "student-" + studentId,
                event
        );
    }
}
