package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.StoredFileInfo;
import com.ibb.yurtlar.dto.StudentDocumentResponse;
import com.ibb.yurtlar.entity.Admission;
import com.ibb.yurtlar.entity.DocumentType;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.entity.StudentDocument;
import com.ibb.yurtlar.entity.TermDocumentRequirement;
import com.ibb.yurtlar.enums.AdmissionStatus;
import com.ibb.yurtlar.enums.StudentDocumentStatus;
import com.ibb.yurtlar.exception.AdmissionNotApprovedException;
import com.ibb.yurtlar.exception.AdmissionNotFoundException;
import com.ibb.yurtlar.exception.DocumentNotRequiredForTermException;
import com.ibb.yurtlar.exception.DocumentUploadClosedException;
import com.ibb.yurtlar.exception.InactiveDocumentTypeException;
import com.ibb.yurtlar.exception.InactiveDormitoryTermException;
import com.ibb.yurtlar.exception.StudentDocumentNotFoundException;
import com.ibb.yurtlar.repository.AdmissionRepository;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.StudentDocumentRepository;
import com.ibb.yurtlar.repository.TermDocumentRequirementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ibb.yurtlar.dto.DownloadedFile;
import org.springframework.core.io.Resource;
import com.ibb.yurtlar.dto.StudentDocumentRequirementStatusResponse;
import com.ibb.yurtlar.dto.DocumentCompletionResponse;
import com.ibb.yurtlar.mapper.StudentDocumentMapper;
import com.ibb.yurtlar.exception.ActiveAdmissionNotFoundForCurrentStudentException;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.exception.InvalidCredentialsException;
import com.ibb.yurtlar.exception.InvalidAdminConfigurationException;
import com.ibb.yurtlar.exception.StudentDocumentAccessDeniedException;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.exception.InvalidUserConfigurationException;
import com.ibb.yurtlar.exception.UserIsNotReviewerException;
import com.ibb.yurtlar.exception.AdmissionAccessDeniedException;

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

    private final StudentDocumentMapper studentDocumentMapper;

    private final AppUserRepository appUserRepository;

    public StudentDocumentService(
            StudentDocumentRepository studentDocumentRepository,
            AdmissionRepository admissionRepository,
            TermDocumentRequirementRepository termDocumentRequirementRepository,
            FileStorageService fileStorageService,
            StudentDocumentMapper studentDocumentMapper,
            AppUserRepository appUserRepository
    ) {
        this.studentDocumentRepository =
                studentDocumentRepository;

        this.admissionRepository =
                admissionRepository;

        this.termDocumentRequirementRepository =
                termDocumentRequirementRepository;

        this.fileStorageService =
                fileStorageService;

        this.studentDocumentMapper =
                studentDocumentMapper;

        this.appUserRepository = appUserRepository;

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
                                () -> new DocumentNotRequiredForTermException(
                                        term.getId(),
                                        documentTypeId
                                )
                        );

        DocumentType documentType =
                requirement.getDocumentType();

        if (!documentType.isActive()) {
            throw new InactiveDocumentTypeException(
                    documentType.getId()
            );
        }

        StoredFileInfo storedFileInfo =
                fileStorageService.storeStudentDocument(
                        file,
                        admission.getId(),
                        documentType.getId()
                );

        Optional<StudentDocument> existingDocument =
                studentDocumentRepository
                        .findByAdmission_IdAndDocumentType_Id(
                                admission.getId(),
                                documentType.getId()
                        );

        if (existingDocument.isPresent()) {
            return replaceExistingDocument(
                    existingDocument.get(),
                    storedFileInfo
            );
        }

        return createNewDocument(
                admission,
                documentType,
                storedFileInfo
        );
    }

    @Transactional(readOnly = true)
    public List<StudentDocumentResponse> getByAdmissionId(
            Long admissionId
    ) {
        if (!admissionRepository.existsById(admissionId)) {
            throw new AdmissionNotFoundException(admissionId);
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

            throw new AdmissionNotApprovedException(
                    admission.getId()
            );
        }

        DormitoryTerm term =
                admission.getDormitoryTerm();

        if (!term.isActive()) {
            throw new InactiveDormitoryTermException(
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
            throw new DocumentUploadClosedException();
        }
    }

    private StudentDocumentResponse createNewDocument(
            Admission admission,
            DocumentType documentType,
            StoredFileInfo storedFileInfo
    ) {
        StudentDocument document =
                new StudentDocument();

        document.setAdmission(admission);
        document.setDocumentType(documentType);
        applyStoredFileInfo(
                document,
                storedFileInfo
        );
        document.setStatus(
                StudentDocumentStatus.UPLOADED
        );

        StudentDocument savedDocument =
                studentDocumentRepository.save(document);

        return studentDocumentMapper
                .toResponse(savedDocument);
    }

    private StudentDocumentResponse replaceExistingDocument(
            StudentDocument document,
            StoredFileInfo storedFileInfo
    ) {
        String oldFilePath =
                document.getFilePath();

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

        studentDocumentRepository.saveAndFlush(document);

        fileStorageService.delete(oldFilePath);

        return studentDocumentMapper
        .toResponse(document);
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
                        () -> new StudentDocumentNotFoundException(
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
            throw new UserIsNotReviewerException(
                    reviewer.getId()
            );
        }

        Dormitory reviewerDormitory =
                reviewer.getDormitory();

        if (reviewerDormitory == null) {
            throw new InvalidUserConfigurationException(
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
                        ActiveAdmissionNotFoundForCurrentStudentException::new
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
                        InvalidCredentialsException::new
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
                    validateDormitoryAccess(
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
            throw new StudentDocumentAccessDeniedException(
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
            throw new StudentDocumentAccessDeniedException(
                    document.getId()
            );
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

        throw new InvalidAdminConfigurationException(
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
                        () -> new AdmissionNotFoundException(
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
                    validateUserDormitoryMatchesAdmission(
                            authenticatedUser,
                            admission
                    );

            case ADMIN ->
                    validateAdminAdmissionAccess(
                            authenticatedUser,
                            admission
                    );

            case STUDENT ->
                    throw new StudentDocumentAccessDeniedException(
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
            throw new AdmissionAccessDeniedException(
                    admission.getId()
            );
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

        throw new InvalidAdminConfigurationException(
                "Admin kullanıcısının yetki kapsamı geçersizdir."
        );
    }
}