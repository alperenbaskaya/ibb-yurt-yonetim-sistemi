package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.CreateDormitoryTermRequest;
import com.ibb.yurtlar.dto.DormitoryTermResponse;
import com.ibb.yurtlar.dto.UpdateDormitoryTermRequest;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditEntityType;
import com.ibb.yurtlar.exception.DormitoryTermAlreadyExistsException;
import com.ibb.yurtlar.exception.DormitoryTermNotFoundException;
import com.ibb.yurtlar.exception.InvalidDateRangeException;
import com.ibb.yurtlar.repository.DormitoryTermRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ibb.yurtlar.exception.DormitoryTermInUseException;
import com.ibb.yurtlar.repository.AdmissionRepository;
import com.ibb.yurtlar.repository.TermDocumentRequirementRepository;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.exception.InvalidCredentialsException;
import com.ibb.yurtlar.exception.UserIsNotAdminException;
import com.ibb.yurtlar.exception.UserManagementAccessDeniedException;

import java.util.List;

@Service
public class DormitoryTermService {

    private final DormitoryTermRepository dormitoryTermRepository;
    private final AdmissionRepository admissionRepository;
    private final TermDocumentRequirementRepository termDocumentRequirementRepository;
    private final AppUserRepository appUserRepository;
    private final AuditLogService auditLogService;


    public DormitoryTermService(
            DormitoryTermRepository dormitoryTermRepository,
            AdmissionRepository admissionRepository,
            TermDocumentRequirementRepository
                    termDocumentRequirementRepository,
            AppUserRepository appUserRepository,
            AuditLogService auditLogService
    ) {
        this.dormitoryTermRepository = dormitoryTermRepository;
        this.admissionRepository = admissionRepository;
        this.termDocumentRequirementRepository =
                termDocumentRequirementRepository;
        this.appUserRepository = appUserRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public DormitoryTermResponse create(
            CreateDormitoryTermRequest request,
            String adminEmail
    ) {
        validateGlobalAdmin(adminEmail);

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
            deactivateCurrentActiveTerms(adminEmail);
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

        auditLogService.recordSystemEvent(adminEmail, AuditAction.TERM_CREATED,
                AuditEntityType.DORMITORY_TERM, savedTerm.getId(), savedTerm.getName(),
                null, savedTerm.getName() + " yurt dönemi oluşturuldu.");

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
            UpdateDormitoryTermRequest request,
            String adminEmail
    ) {
        validateGlobalAdmin(adminEmail);

        DormitoryTerm term = findTermById(id);
        boolean previouslyActive = term.isActive();

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
            deactivateCurrentActiveTerms(adminEmail);
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

        auditLogService.recordSystemEvent(adminEmail, AuditAction.TERM_UPDATED,
                AuditEntityType.DORMITORY_TERM, updatedTerm.getId(), updatedTerm.getName(),
                null, updatedTerm.getName() + " yurt dönemi güncellendi.");
        recordTermActiveTransition(adminEmail, updatedTerm, previouslyActive);

        return toResponse(updatedTerm);
    }

    private DormitoryTerm findTermById(Long id) {
        return dormitoryTermRepository.findById(id)
                .orElseThrow(
                        () -> new DormitoryTermNotFoundException(id)
                );
    }

    private void deactivateCurrentActiveTerms(String adminEmail) {
        List<DormitoryTerm> activeTerms =
                dormitoryTermRepository.findAllByActiveTrue();

        activeTerms.forEach(term -> {
            term.setActive(false);
            auditLogService.recordSystemEvent(adminEmail, AuditAction.TERM_DEACTIVATED,
                    AuditEntityType.DORMITORY_TERM, term.getId(), term.getName(), null,
                    term.getName() + " yurt dönemi pasif hale getirildi.");
        });
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
    public void delete(Long id, String adminEmail) {
        validateGlobalAdmin(adminEmail);

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

        auditLogService.recordSystemEvent(adminEmail, AuditAction.TERM_DELETED,
                AuditEntityType.DORMITORY_TERM, term.getId(), term.getName(), null,
                term.getName() + " yurt dönemi silindi.");
    }

    @Transactional
    public DormitoryTermResponse updateActiveStatus(
            Long id,
            Boolean active,
            String adminEmail
    ) {
        validateGlobalAdmin(adminEmail);

        DormitoryTerm term = findTermById(id);
        boolean previouslyActive = term.isActive();

        if (active && !term.isActive()) {
            deactivateCurrentActiveTerms(adminEmail);
        }

        term.setActive(active);

        recordTermActiveTransition(adminEmail, term, previouslyActive);

        return toResponse(term);
    }

    private void recordTermActiveTransition(String adminEmail,
                                            DormitoryTerm term,
                                            boolean previouslyActive) {
        if (previouslyActive == term.isActive()) {
            return;
        }
        AuditAction action = term.isActive()
                ? AuditAction.TERM_ACTIVATED : AuditAction.TERM_DEACTIVATED;
        auditLogService.recordSystemEvent(adminEmail, action,
                AuditEntityType.DORMITORY_TERM, term.getId(), term.getName(), null,
                term.getName() + (term.isActive()
                        ? " yurt dönemi aktif hale getirildi."
                        : " yurt dönemi pasif hale getirildi."));
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
