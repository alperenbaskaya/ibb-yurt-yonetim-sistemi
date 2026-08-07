package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.CreateDocumentTypeRequest;
import com.ibb.yurtlar.dto.DocumentTypeResponse;
import com.ibb.yurtlar.dto.UpdateDocumentTypeRequest;
import com.ibb.yurtlar.entity.DocumentType;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.exception.DocumentTypeAlreadyExistsException;
import com.ibb.yurtlar.exception.DocumentTypeNotFoundException;
import com.ibb.yurtlar.repository.DocumentTypeRepository;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.exception.InvalidCredentialsException;
import com.ibb.yurtlar.exception.UserIsNotAdminException;
import com.ibb.yurtlar.exception.UserManagementAccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocumentTypeService {

    private final DocumentTypeRepository documentTypeRepository;
    private final AppUserRepository appUserRepository;

    public DocumentTypeService(
            DocumentTypeRepository documentTypeRepository,
            AppUserRepository appUserRepository
    ) {
        this.documentTypeRepository = documentTypeRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public DocumentTypeResponse create(
            CreateDocumentTypeRequest request,
            String adminEmail
    ) {
        validateGlobalAdmin(adminEmail);

        String normalizedName = normalizeName(request.name());

        if (documentTypeRepository
                .existsByNameIgnoreCase(normalizedName)) {

            throw new DocumentTypeAlreadyExistsException(
                    normalizedName
            );
        }

        DocumentType documentType = new DocumentType();
        documentType.setName(normalizedName);
        documentType.setDescription(
                normalizeDescription(request.description())
        );
        documentType.setActive(true);

        DocumentType savedDocumentType =
                documentTypeRepository.save(documentType);

        return toResponse(savedDocumentType);
    }

    @Transactional(readOnly = true)
    public List<DocumentTypeResponse> getAll() {
        return documentTypeRepository
                .findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentTypeResponse> getAllActive() {
        return documentTypeRepository
                .findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentTypeResponse getById(Long id) {
        return toResponse(findDocumentTypeById(id));
    }

    @Transactional
    public DocumentTypeResponse update(
            Long id,
            UpdateDocumentTypeRequest request,
            String adminEmail
    ) {
        validateGlobalAdmin(adminEmail);

        DocumentType documentType =
                findDocumentTypeById(id);

        String normalizedName =
                normalizeName(request.name());

        boolean anotherDocumentTypeUsesName =
                documentTypeRepository
                        .existsByNameIgnoreCaseAndIdNot(
                                normalizedName,
                                id
                        );

        if (anotherDocumentTypeUsesName) {
            throw new DocumentTypeAlreadyExistsException(
                    normalizedName
            );
        }

        documentType.setName(normalizedName);
        documentType.setDescription(
                normalizeDescription(request.description())
        );
        documentType.setActive(request.active());

        return toResponse(documentType);
    }

    private DocumentType findDocumentTypeById(Long id) {
        return documentTypeRepository
                .findById(id)
                .orElseThrow(
                        () -> new DocumentTypeNotFoundException(id)
                );
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private String normalizeDescription(
            String description
    ) {
        if (description == null) {
            return null;
        }

        String trimmedDescription = description.trim();

        if (trimmedDescription.isEmpty()) {
            return null;
        }

        return trimmedDescription;
    }

    private DocumentTypeResponse toResponse(
            DocumentType documentType
    ) {
        return new DocumentTypeResponse(
                documentType.getId(),
                documentType.getName(),
                documentType.getDescription(),
                documentType.isActive(),
                documentType.getCreatedAt(),
                documentType.getUpdatedAt()
        );
    }

    private void validateGlobalAdmin(String email) {
        AppUser admin = appUserRepository
                .findByNormalizedEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (admin.getRole() != Role.ADMIN) {
            throw new UserIsNotAdminException(admin.getId());
        }

        if (admin.getAdminScope() != AdminScope.GLOBAL) {
            throw new UserManagementAccessDeniedException(
                    "Bu işlem yalnızca GLOBAL adminler tarafından yapılabilir."
            );
        }
    }
}
