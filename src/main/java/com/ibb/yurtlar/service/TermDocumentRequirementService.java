package com.ibb.yurtlar.service;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.*;

import com.ibb.yurtlar.exception.BusinessException;

import com.ibb.yurtlar.dto.CreateTermDocumentRequirementRequest;
import com.ibb.yurtlar.dto.TermDocumentRequirementResponse;
import com.ibb.yurtlar.dto.UpdateTermDocumentRequirementRequest;
import com.ibb.yurtlar.entity.DocumentType;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.entity.TermDocumentRequirement;
import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.DocumentTypeRepository;
import com.ibb.yurtlar.repository.DormitoryTermRepository;
import com.ibb.yurtlar.repository.TermDocumentRequirementRepository;
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditEntityType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TermDocumentRequirementService {

    private final TermDocumentRequirementRepository
            termDocumentRequirementRepository;

    private final DormitoryTermRepository dormitoryTermRepository;

    private final DocumentTypeRepository documentTypeRepository;
    private final AppUserRepository appUserRepository;
    private final AuditLogService auditLogService;

    public TermDocumentRequirementService(
            TermDocumentRequirementRepository
                    termDocumentRequirementRepository,
            DormitoryTermRepository dormitoryTermRepository,
            DocumentTypeRepository documentTypeRepository,
            AppUserRepository appUserRepository,
            AuditLogService auditLogService
    ) {
        this.termDocumentRequirementRepository =
                termDocumentRequirementRepository;

        this.dormitoryTermRepository =
                dormitoryTermRepository;

        this.documentTypeRepository =
                documentTypeRepository;
        this.appUserRepository = appUserRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public TermDocumentRequirementResponse create(
            CreateTermDocumentRequirementRequest request,
            String adminEmail
    ) {
        validateGlobalAdmin(adminEmail);

        DormitoryTerm dormitoryTerm =
                dormitoryTermRepository
                        .findById(request.dormitoryTermId())
                        .orElseThrow(
                                () -> new BusinessException(DORMITORY_TERM_NOT_FOUND,
                                        request.dormitoryTermId()
                                )
                        );

        DocumentType documentType =
                documentTypeRepository
                        .findById(request.documentTypeId())
                        .orElseThrow(
                                () -> new BusinessException(DOCUMENT_TYPE_NOT_FOUND,
                                        request.documentTypeId()
                                )
                        );

        if (!documentType.isActive()) {
            throw new BusinessException(INACTIVE_DOCUMENT_TYPE,
                    documentType.getId()
            );
        }

        boolean requirementExists =
                termDocumentRequirementRepository
                        .existsByDormitoryTerm_IdAndDocumentType_Id(
                                dormitoryTerm.getId(),
                                documentType.getId()
                        );

        if (requirementExists) {
            throw new BusinessException(TERM_DOCUMENT_REQUIREMENT_ALREADY_EXISTS,
                    dormitoryTerm.getId(),
                    documentType.getId()
            );
        }

        TermDocumentRequirement requirement =
                new TermDocumentRequirement();

        requirement.setDormitoryTerm(dormitoryTerm);
        requirement.setDocumentType(documentType);
        requirement.setRequired(request.required());

        TermDocumentRequirement savedRequirement =
                termDocumentRequirementRepository.save(requirement);

        auditLogService.recordSystemEvent(adminEmail, AuditAction.REQUIREMENT_CREATED,
                AuditEntityType.TERM_DOCUMENT_REQUIREMENT, savedRequirement.getId(),
                documentType.getName(), null, dormitoryTerm.getName() + " dönemi için "
                        + documentType.getName() + " gereksinimi oluşturuldu.");

        return toResponse(savedRequirement);
    }

    @Transactional(readOnly = true)
    public List<TermDocumentRequirementResponse> getAllByTerm(
            Long dormitoryTermId
    ) {
        validateDormitoryTermExists(dormitoryTermId);

        return termDocumentRequirementRepository
                .findAllByDormitoryTerm_IdOrderByDocumentType_NameAsc(
                        dormitoryTermId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TermDocumentRequirementResponse>
    getActiveRequirementsByTerm(
            Long dormitoryTermId
    ) {
        validateDormitoryTermExists(dormitoryTermId);

        return termDocumentRequirementRepository
                .findAllByDormitoryTerm_IdAndDocumentType_ActiveTrueOrderByDocumentType_NameAsc(
                        dormitoryTermId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TermDocumentRequirementResponse getById(
            Long id
    ) {
        return toResponse(findRequirementById(id));
    }

    @Transactional
    public TermDocumentRequirementResponse update(
            Long id,
            UpdateTermDocumentRequirementRequest request,
            String adminEmail
    ) {
        validateGlobalAdmin(adminEmail);

        TermDocumentRequirement requirement =
                findRequirementById(id);

        requirement.setRequired(request.required());

        auditLogService.recordSystemEvent(adminEmail, AuditAction.REQUIREMENT_UPDATED,
                AuditEntityType.TERM_DOCUMENT_REQUIREMENT, requirement.getId(),
                requirement.getDocumentType().getName(), null,
                requirement.getDormitoryTerm().getName() + " dönemi belge gereksinimi güncellendi.");

        return toResponse(requirement);
    }

    private TermDocumentRequirement findRequirementById(
            Long id
    ) {
        return termDocumentRequirementRepository
                .findById(id)
                .orElseThrow(
                        () -> new BusinessException(TERM_DOCUMENT_REQUIREMENT_NOT_FOUND,
                                id
                        )
                );
    }

    private void validateDormitoryTermExists(
            Long dormitoryTermId
    ) {
        if (!dormitoryTermRepository.existsById(dormitoryTermId)) {
            throw new BusinessException(DORMITORY_TERM_NOT_FOUND,
                    dormitoryTermId
            );
        }
    }

    private TermDocumentRequirementResponse toResponse(
            TermDocumentRequirement requirement
    ) {
        DormitoryTerm term =
                requirement.getDormitoryTerm();

        DocumentType documentType =
                requirement.getDocumentType();

        return new TermDocumentRequirementResponse(
                requirement.getId(),

                term.getId(),
                term.getName(),

                documentType.getId(),
                documentType.getName(),
                documentType.getDescription(),

                documentType.isActive(),
                requirement.isRequired(),

                requirement.getCreatedAt(),
                requirement.getUpdatedAt()
        );
    }

    private void validateGlobalAdmin(String email) {
        AppUser admin = appUserRepository
                .findByNormalizedEmail(email)
                .orElseThrow(() -> new BusinessException(INVALID_CREDENTIALS));

        if (admin.getRole() != Role.ADMIN) {
            throw new BusinessException(USER_IS_NOT_ADMIN, admin.getId());
        }

        if (admin.getAdminScope() != AdminScope.GLOBAL) {
            throw new BusinessException(USER_MANAGEMENT_ACCESS_DENIED,
                    "Bu işlem yalnızca GLOBAL adminler tarafından yapılabilir."
            );
        }
    }
}
