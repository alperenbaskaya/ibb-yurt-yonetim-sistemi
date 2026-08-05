package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.CreateTermDocumentRequirementRequest;
import com.ibb.yurtlar.dto.TermDocumentRequirementResponse;
import com.ibb.yurtlar.dto.UpdateTermDocumentRequirementRequest;
import com.ibb.yurtlar.entity.DocumentType;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.entity.TermDocumentRequirement;
import com.ibb.yurtlar.exception.DocumentTypeNotFoundException;
import com.ibb.yurtlar.exception.DormitoryTermNotFoundException;
import com.ibb.yurtlar.exception.InactiveDocumentTypeException;
import com.ibb.yurtlar.exception.TermDocumentRequirementAlreadyExistsException;
import com.ibb.yurtlar.exception.TermDocumentRequirementNotFoundException;
import com.ibb.yurtlar.repository.DocumentTypeRepository;
import com.ibb.yurtlar.repository.DormitoryTermRepository;
import com.ibb.yurtlar.repository.TermDocumentRequirementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TermDocumentRequirementService {

    private final TermDocumentRequirementRepository
            termDocumentRequirementRepository;

    private final DormitoryTermRepository dormitoryTermRepository;

    private final DocumentTypeRepository documentTypeRepository;

    public TermDocumentRequirementService(
            TermDocumentRequirementRepository
                    termDocumentRequirementRepository,
            DormitoryTermRepository dormitoryTermRepository,
            DocumentTypeRepository documentTypeRepository
    ) {
        this.termDocumentRequirementRepository =
                termDocumentRequirementRepository;

        this.dormitoryTermRepository =
                dormitoryTermRepository;

        this.documentTypeRepository =
                documentTypeRepository;
    }

    @Transactional
    public TermDocumentRequirementResponse create(
            CreateTermDocumentRequirementRequest request
    ) {
        DormitoryTerm dormitoryTerm =
                dormitoryTermRepository
                        .findById(request.dormitoryTermId())
                        .orElseThrow(
                                () -> new DormitoryTermNotFoundException(
                                        request.dormitoryTermId()
                                )
                        );

        DocumentType documentType =
                documentTypeRepository
                        .findById(request.documentTypeId())
                        .orElseThrow(
                                () -> new DocumentTypeNotFoundException(
                                        request.documentTypeId()
                                )
                        );

        if (!documentType.isActive()) {
            throw new InactiveDocumentTypeException(
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
            throw new TermDocumentRequirementAlreadyExistsException(
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
            UpdateTermDocumentRequirementRequest request
    ) {
        TermDocumentRequirement requirement =
                findRequirementById(id);

        requirement.setRequired(request.required());

        return toResponse(requirement);
    }

    private TermDocumentRequirement findRequirementById(
            Long id
    ) {
        return termDocumentRequirementRepository
                .findById(id)
                .orElseThrow(
                        () -> new TermDocumentRequirementNotFoundException(
                                id
                        )
                );
    }

    private void validateDormitoryTermExists(
            Long dormitoryTermId
    ) {
        if (!dormitoryTermRepository.existsById(dormitoryTermId)) {
            throw new DormitoryTermNotFoundException(
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
}