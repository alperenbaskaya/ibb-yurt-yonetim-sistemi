package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.CreateDormitoryTermRequest;
import com.ibb.yurtlar.dto.DormitoryTermResponse;
import com.ibb.yurtlar.dto.UpdateDormitoryTermRequest;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.exception.DormitoryTermAlreadyExistsException;
import com.ibb.yurtlar.exception.DormitoryTermNotFoundException;
import com.ibb.yurtlar.exception.InvalidDateRangeException;
import com.ibb.yurtlar.repository.DormitoryTermRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ibb.yurtlar.exception.DormitoryTermInUseException;
import com.ibb.yurtlar.repository.AdmissionRepository;
import com.ibb.yurtlar.repository.TermDocumentRequirementRepository;

import java.util.List;

@Service
public class DormitoryTermService {

    private final DormitoryTermRepository dormitoryTermRepository;
    private final AdmissionRepository admissionRepository;
    private final TermDocumentRequirementRepository termDocumentRequirementRepository;


    public DormitoryTermService(
            DormitoryTermRepository dormitoryTermRepository,
            AdmissionRepository admissionRepository,
            TermDocumentRequirementRepository
                    termDocumentRequirementRepository
    ) {
        this.dormitoryTermRepository = dormitoryTermRepository;
        this.admissionRepository = admissionRepository;
        this.termDocumentRequirementRepository =
                termDocumentRequirementRepository;
    }

    @Transactional
    public DormitoryTermResponse create(
            CreateDormitoryTermRequest request
    ) {
        if (dormitoryTermRepository.existsByName(request.name())) {
            throw new DormitoryTermAlreadyExistsException(request.name());
        }

        validateDates(
                request.startDate(),
                request.endDate(),
                request.documentUploadStartDate(),
                request.documentUploadEndDate()
        );

        if (request.active()) {
            deactivateCurrentActiveTerms();
        }

        DormitoryTerm term = new DormitoryTerm();
        term.setName(request.name());
        term.setStartDate(request.startDate());
        term.setEndDate(request.endDate());
        term.setDocumentUploadStartDate(
                request.documentUploadStartDate()
        );
        term.setDocumentUploadEndDate(
                request.documentUploadEndDate()
        );
        term.setActive(request.active());

        DormitoryTerm savedTerm =
                dormitoryTermRepository.save(term);

        return toResponse(savedTerm);
    }

    @Transactional(readOnly = true)
    public List<DormitoryTermResponse> getAll() {
        return dormitoryTermRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DormitoryTermResponse getById(Long id) {
        DormitoryTerm term = findTermById(id);

        return toResponse(term);
    }

    @Transactional
    public DormitoryTermResponse update(
            Long id,
            UpdateDormitoryTermRequest request
    ) {
        DormitoryTerm term = findTermById(id);

        if (dormitoryTermRepository
                .existsByNameAndIdNot(request.name(), id)) {

            throw new DormitoryTermAlreadyExistsException(
                    request.name()
            );
        }

        validateDates(
                request.startDate(),
                request.endDate(),
                request.documentUploadStartDate(),
                request.documentUploadEndDate()
        );

        if (request.active() && !term.isActive()) {
            deactivateCurrentActiveTerms();
        }

        term.setName(request.name());
        term.setStartDate(request.startDate());
        term.setEndDate(request.endDate());
        term.setDocumentUploadStartDate(
                request.documentUploadStartDate()
        );
        term.setDocumentUploadEndDate(
                request.documentUploadEndDate()
        );
        term.setActive(request.active());

        DormitoryTerm updatedTerm =
                dormitoryTermRepository.save(term);

        return toResponse(updatedTerm);
    }

    private DormitoryTerm findTermById(Long id) {
        return dormitoryTermRepository.findById(id)
                .orElseThrow(
                        () -> new DormitoryTermNotFoundException(id)
                );
    }

    private void deactivateCurrentActiveTerms() {
        List<DormitoryTerm> activeTerms =
                dormitoryTermRepository.findAllByActiveTrue();

        activeTerms.forEach(term -> term.setActive(false));
    }

    private void validateDates(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            java.time.LocalDate documentUploadStartDate,
            java.time.LocalDate documentUploadEndDate
    ) {
        if (endDate.isBefore(startDate)) {
            throw new InvalidDateRangeException(
                    "Dönem bitiş tarihi başlangıç tarihinden önce olamaz."
            );
        }

        if (documentUploadEndDate
                .isBefore(documentUploadStartDate)) {

            throw new InvalidDateRangeException(
                    "Belge yükleme bitiş tarihi başlangıç tarihinden önce olamaz."
            );
        }

        if (documentUploadStartDate.isBefore(startDate)) {
            throw new InvalidDateRangeException(
                    "Belge yükleme başlangıç tarihi dönem başlangıcından önce olamaz."
            );
        }

        if (documentUploadEndDate.isAfter(endDate)) {
            throw new InvalidDateRangeException(
                    "Belge yükleme bitiş tarihi dönem bitişinden sonra olamaz."
            );
        }
    }

    private DormitoryTermResponse toResponse(
            DormitoryTerm term
    ) {
        return new DormitoryTermResponse(
                term.getId(),
                term.getName(),
                term.getStartDate(),
                term.getEndDate(),
                term.getDocumentUploadStartDate(),
                term.getDocumentUploadEndDate(),
                term.isActive()
        );
    }

    @Transactional
    public void delete(Long id) {
        DormitoryTerm term = findTermById(id);

        boolean hasAdmissions =
                admissionRepository.existsByDormitoryTerm_Id(id);

        boolean hasDocumentRequirements =
                termDocumentRequirementRepository
                        .existsByDormitoryTerm_Id(id);

        if (hasAdmissions || hasDocumentRequirements) {
            throw new DormitoryTermInUseException(id);
        }

        dormitoryTermRepository.delete(term);
    }

    @Transactional
    public DormitoryTermResponse updateActiveStatus(
            Long id,
            Boolean active
    ) {
        DormitoryTerm term = findTermById(id);

        if (active && !term.isActive()) {
            deactivateCurrentActiveTerms();
        }

        term.setActive(active);

        return toResponse(term);
    }


}