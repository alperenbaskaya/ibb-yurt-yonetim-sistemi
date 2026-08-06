package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.AdmissionResponse;
import com.ibb.yurtlar.dto.CreateAdmissionRequest;
import com.ibb.yurtlar.dto.UpdateAdmissionStatusRequest;
import com.ibb.yurtlar.entity.Admission;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.entity.Student;
import com.ibb.yurtlar.enums.AdmissionStatus;
import com.ibb.yurtlar.exception.AdmissionAlreadyExistsException;
import com.ibb.yurtlar.exception.AdmissionNotFoundException;
import com.ibb.yurtlar.exception.DormitoryTermNotFoundException;
import com.ibb.yurtlar.exception.StudentNotFoundException;
import com.ibb.yurtlar.repository.AdmissionRepository;
import com.ibb.yurtlar.repository.DormitoryTermRepository;
import com.ibb.yurtlar.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ibb.yurtlar.exception.ActiveDormitoryTermNotFoundException;
import com.ibb.yurtlar.dto.CurrentTermAdmissionSummaryResponse;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.exception.DormitoryNotFoundException;
import com.ibb.yurtlar.exception.InactiveDormitoryException;
import com.ibb.yurtlar.repository.DormitoryRepository;
import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.exception.AdmissionAccessDeniedException;
import com.ibb.yurtlar.exception.InvalidAdminConfigurationException;
import com.ibb.yurtlar.exception.InvalidCredentialsException;
import com.ibb.yurtlar.exception.UserIsNotAdminException;
import com.ibb.yurtlar.repository.AppUserRepository;

import java.util.List;

@Service
public class AdmissionService {

    private final AdmissionRepository admissionRepository;
    private final StudentRepository studentRepository;
    private final DormitoryTermRepository dormitoryTermRepository;
    private final DormitoryRepository dormitoryRepository;
    private final AppUserRepository appUserRepository;

    public AdmissionService(
            AdmissionRepository admissionRepository,
            StudentRepository studentRepository,
            DormitoryTermRepository dormitoryTermRepository,
            DormitoryRepository dormitoryRepository,
            AppUserRepository appUserRepository
    ) {
        this.admissionRepository = admissionRepository;
        this.studentRepository = studentRepository;
        this.dormitoryTermRepository = dormitoryTermRepository;
        this.dormitoryRepository = dormitoryRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public AdmissionResponse create(
            CreateAdmissionRequest request,
            String adminEmail
    ) {
        AppUser admin =
                findAuthenticatedAdmin(
                        adminEmail
                );

        Student student =
                studentRepository
                        .findById(request.studentId())
                        .orElseThrow(
                                () -> new StudentNotFoundException(
                                        request.studentId()
                                )
                        );

        DormitoryTerm dormitoryTerm =
                dormitoryTermRepository
                        .findById(request.dormitoryTermId())
                        .orElseThrow(
                                () -> new DormitoryTermNotFoundException(
                                        request.dormitoryTermId()
                                )
                        );

        boolean admissionExists =
                admissionRepository
                        .existsByStudent_IdAndDormitoryTerm_Id(
                                student.getId(),
                                dormitoryTerm.getId()
                        );

        if (admissionExists) {
            throw new AdmissionAlreadyExistsException(
                    student.getId(),
                    dormitoryTerm.getId()
            );
        }

        Dormitory dormitory =
                dormitoryRepository
                        .findById(request.dormitoryId())
                        .orElseThrow(
                                () -> new DormitoryNotFoundException(
                                        request.dormitoryId()
                                )
                        );

        validateAdminDormitoryAccess(
                admin,
                dormitory.getId(),
                null
        );

        if (!dormitory.isActive()) {
            throw new InactiveDormitoryException(
                    dormitory.getId()
            );
        }

        Admission admission =
                new Admission();

        admission.setStudent(student);
        admission.setDormitoryTerm(dormitoryTerm);
        admission.setDormitory(dormitory);
        admission.setAdmissionDate(
                request.admissionDate()
        );
        admission.setStatus(
                AdmissionStatus.PENDING
        );

        Admission savedAdmission =
                admissionRepository.save(
                        admission
                );

        return toResponse(
                savedAdmission
        );
    }

    @Transactional(readOnly = true)
    public List<AdmissionResponse> getAll(
            String adminEmail
    ) {
        AppUser admin =
                findAuthenticatedAdmin(
                        adminEmail
                );

        Long adminDormitoryId =
                resolveAdminDormitoryId(
                        admin
                );

        return admissionRepository
                .findByAdminScopeAndFilters(
                        null,
                        null,
                        null,
                        adminDormitoryId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdmissionResponse getById(
            Long id,
            String adminEmail
    ) {
        AppUser admin =
                findAuthenticatedAdmin(
                        adminEmail
                );

        Admission admission =
                findAdmissionById(
                        id
                );

        validateAdminAdmissionAccess(
                admin,
                admission
        );

        return toResponse(
                admission
        );
    }

    @Transactional(readOnly = true)
    public List<AdmissionResponse> getByStudentId(
            Long studentId,
            String adminEmail
    ) {
        AppUser admin =
                findAuthenticatedAdmin(
                        adminEmail
                );

        if (!studentRepository.existsById(studentId)) {
            throw new StudentNotFoundException(
                    studentId
            );
        }

        Long adminDormitoryId =
                resolveAdminDormitoryId(
                        admin
                );

        return admissionRepository
                .findByAdminScopeAndFilters(
                        null,
                        null,
                        null,
                        adminDormitoryId
                )
                .stream()
                .filter(admission ->
                        admission
                                .getStudent()
                                .getId()
                                .equals(studentId)
                )
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AdmissionResponse updateStatus(
            Long id,
            UpdateAdmissionStatusRequest request,
            String adminEmail
    ) {
        AppUser admin =
                findAuthenticatedAdmin(
                        adminEmail
                );

        Admission admission =
                findAdmissionById(
                        id
                );

        validateAdminAdmissionAccess(
                admin,
                admission
        );

        admission.setStatus(
                request.status()
        );

        return toResponse(
                admission
        );
    }

    private Admission findAdmissionById(Long id) {
        return admissionRepository.findById(id)
                .orElseThrow(
                        () -> new AdmissionNotFoundException(id)
                );
    }

    private AdmissionResponse toResponse(
            Admission admission
    ) {
        Student student =
                admission.getStudent();

        AppUser user =
                student.getUser();

        DormitoryTerm term =
                admission.getDormitoryTerm();

        Dormitory dormitory =
                admission.getDormitory();

        return new AdmissionResponse(
                admission.getId(),

                student.getId(),
                user.getFirstName(),
                user.getLastName(),
                student.getIdentityNumber(),

                term.getId(),
                term.getName(),

                dormitory.getId(),
                dormitory.getName(),

                admission.getAdmissionDate(),
                admission.getStatus(),

                admission.getCreatedAt(),
                admission.getUpdatedAt()
        );
    }

    @Transactional
    public void delete(
            Long id,
            String adminEmail
    ) {
        AppUser admin =
                findAuthenticatedAdmin(
                        adminEmail
                );

        Admission admission =
                findAdmissionById(
                        id
                );

        validateAdminAdmissionAccess(
                admin,
                admission
        );

        admissionRepository.delete(
                admission
        );
    }

    @Transactional(readOnly = true)
    public List<AdmissionResponse> filter(
            Long termId,
            AdmissionStatus status,
            String dormitoryName,
            String adminEmail
    ) {
        AppUser admin =
                findAuthenticatedAdmin(
                        adminEmail
                );

        if (termId != null
                && !dormitoryTermRepository
                .existsById(termId)) {

            throw new DormitoryTermNotFoundException(
                    termId
            );
        }

        Long adminDormitoryId =
                resolveAdminDormitoryId(
                        admin
                );

        return admissionRepository
                .findByAdminScopeAndFilters(
                        termId,
                        status,
                        normalizeOptionalText(
                                dormitoryName
                        ),
                        adminDormitoryId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();

        return trimmedValue.isEmpty()
                ? null
                : trimmedValue;
    }

    @Transactional(readOnly = true)
    public List<AdmissionResponse> getCurrentTermAdmissions(
            AdmissionStatus status,
            String dormitoryName
    ) {
        DormitoryTerm activeTerm =
                dormitoryTermRepository
                        .findByActiveTrue()
                        .orElseThrow(
                                ActiveDormitoryTermNotFoundException::new
                        );

        return admissionRepository
                .findByFilters(
                        activeTerm.getId(),
                        status,
                        normalizeOptionalText(dormitoryName)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CurrentTermAdmissionSummaryResponse
    getCurrentTermSummary(
            String adminEmail
    ) {
        AppUser admin =
                findAuthenticatedAdmin(
                        adminEmail
                );

        DormitoryTerm activeTerm =
                dormitoryTermRepository
                        .findByActiveTrue()
                        .orElseThrow(
                                ActiveDormitoryTermNotFoundException::new
                        );

        Long termId =
                activeTerm.getId();

        Long adminDormitoryId =
                resolveAdminDormitoryId(
                        admin
                );

        long pendingCount =
                admissionRepository
                        .countByTermAndStatusAndAdminScope(
                                termId,
                                AdmissionStatus.PENDING,
                                adminDormitoryId
                        );

        long approvedCount =
                admissionRepository
                        .countByTermAndStatusAndAdminScope(
                                termId,
                                AdmissionStatus.APPROVED,
                                adminDormitoryId
                        );

        long rejectedCount =
                admissionRepository
                        .countByTermAndStatusAndAdminScope(
                                termId,
                                AdmissionStatus.REJECTED,
                                adminDormitoryId
                        );

        long totalCount =
                pendingCount
                        + approvedCount
                        + rejectedCount;

        return new CurrentTermAdmissionSummaryResponse(
                termId,
                activeTerm.getName(),
                totalCount,
                pendingCount,
                approvedCount,
                rejectedCount
        );
    }

    @Transactional(readOnly = true)
    public List<AdmissionResponse>
    getCurrentTermAdmissionsByDormitory(
            String dormitoryName,
            AdmissionStatus status,
            String adminEmail
    ) {
        AppUser admin =
                findAuthenticatedAdmin(
                        adminEmail
                );

        DormitoryTerm activeTerm =
                dormitoryTermRepository
                        .findByActiveTrue()
                        .orElseThrow(
                                ActiveDormitoryTermNotFoundException::new
                        );

        Long adminDormitoryId =
                resolveAdminDormitoryId(
                        admin
                );

        return admissionRepository
                .findByAdminScopeAndFilters(
                        activeTerm.getId(),
                        status,
                        normalizeOptionalText(
                                dormitoryName
                        ),
                        adminDormitoryId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private AppUser findAuthenticatedAdmin(
            String email
    ) {
        AppUser admin =
                appUserRepository
                        .findByNormalizedEmail(
                                email
                        )
                        .orElseThrow(
                                InvalidCredentialsException::new
                        );

        if (admin.getRole() != Role.ADMIN) {
            throw new UserIsNotAdminException(
                    admin.getId()
            );
        }

        if (!admin.isActive()) {
            throw new InvalidCredentialsException();
        }

        return admin;
    }

    private Long resolveAdminDormitoryId(
            AppUser admin
    ) {
        if (admin.getAdminScope()
                == AdminScope.GLOBAL) {

            return null;
        }

        if (admin.getAdminScope()
                == AdminScope.DORMITORY) {

            Dormitory dormitory =
                    admin.getDormitory();

            if (dormitory == null) {
                throw new InvalidAdminConfigurationException(
                        "Yurt admini için yurt ataması zorunludur."
                );
            }

            return dormitory.getId();
        }

        throw new InvalidAdminConfigurationException(
                "Admin kullanıcısının yetki kapsamı geçersizdir."
        );
    }

    private void validateAdminAdmissionAccess(
            AppUser admin,
            Admission admission
    ) {
        validateAdminDormitoryAccess(
                admin,
                admission.getDormitory().getId(),
                admission.getId()
        );
    }

    private void validateAdminDormitoryAccess(
            AppUser admin,
            Long targetDormitoryId,
            Long admissionId
    ) {
        if (admin.getAdminScope()
                == AdminScope.GLOBAL) {

            return;
        }

        Long adminDormitoryId =
                resolveAdminDormitoryId(
                        admin
                );

        if (!adminDormitoryId.equals(
                targetDormitoryId
        )) {
            if (admissionId != null) {
                throw new AdmissionAccessDeniedException(
                        admissionId
                );
            }

            throw new AdmissionAccessDeniedException(
                    "Bu yurt için kabul kaydı oluşturma yetkiniz bulunmamaktadır."
            );
        }
    }

}