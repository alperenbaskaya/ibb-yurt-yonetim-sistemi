package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.AdmissionResponse;
import com.ibb.yurtlar.dto.CreateAdmissionRequest;
import com.ibb.yurtlar.dto.UpdateAdmissionStatusRequest;
import com.ibb.yurtlar.dto.AdmissionPageResponse;
import com.ibb.yurtlar.dto.BulkApproveAdmissionsRequest;
import com.ibb.yurtlar.dto.BulkApproveAdmissionsResponse;
import com.ibb.yurtlar.entity.Admission;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.entity.Student;
import com.ibb.yurtlar.enums.AdmissionStatus;
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.AuditEntityType;
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
import com.ibb.yurtlar.exception.InvalidAdmissionStatusTransitionException;
import com.ibb.yurtlar.exception.InvalidCredentialsException;
import com.ibb.yurtlar.exception.UserIsNotAdminException;
import com.ibb.yurtlar.exception.InvalidAdmissionRequestException;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.enums.NotificationReferenceType;
import com.ibb.yurtlar.enums.NotificationType;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;

@Service
public class AdmissionService {

    private final AdmissionRepository admissionRepository;
    private final StudentRepository studentRepository;
    private final DormitoryTermRepository dormitoryTermRepository;
    private final DormitoryRepository dormitoryRepository;
    private final AppUserRepository appUserRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public AdmissionService(
            AdmissionRepository admissionRepository,
            StudentRepository studentRepository,
            DormitoryTermRepository dormitoryTermRepository,
            DormitoryRepository dormitoryRepository,
            AppUserRepository appUserRepository,
            NotificationService notificationService,
            AuditLogService auditLogService
    ) {
        this.admissionRepository = admissionRepository;
        this.studentRepository = studentRepository;
        this.dormitoryTermRepository = dormitoryTermRepository;
        this.dormitoryRepository = dormitoryRepository;
        this.appUserRepository = appUserRepository;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;

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

        Map<Long, List<AppUser>> dormitoryAdmins =
                admin.getAdminScope() == AdminScope.GLOBAL
                        && request.status() == AdmissionStatus.APPROVED
                        ? loadDormitoryAdmins(List.of(admission))
                        : Collections.emptyMap();
        applyStatusChange(admission, request.status(), adminEmail, dormitoryAdmins);

        return toResponse(
                admission
        );
    }

    @Transactional(readOnly = true)
    public AdmissionPageResponse getGlobalCurrentTermAdmissions(
            int page, int size, AdmissionStatus status, String adminEmail
    ) {
        validatePage(page, size);
        requireGlobalAdmin(adminEmail);
        DormitoryTerm activeTerm = findActiveTerm();
        Page<Admission> admissionPage = admissionRepository.findGlobalCurrentTermPage(
                activeTerm.getId(), status,
                PageRequest.of(page, size, Sort.by(
                        Sort.Order.desc("createdAt"), Sort.Order.desc("id")
                ))
        );

        return new AdmissionPageResponse(
                admissionPage.getContent().stream().map(this::toResponse).toList(),
                admissionPage.getNumber(), admissionPage.getSize(),
                admissionPage.getTotalElements(), admissionPage.getTotalPages(),
                admissionPage.isFirst(), admissionPage.isLast()
        );
    }

    @Transactional
    public BulkApproveAdmissionsResponse approveSelectedCurrentTermAdmissions(
            BulkApproveAdmissionsRequest request, String adminEmail
    ) {
        requireGlobalAdmin(adminEmail);
        DormitoryTerm activeTerm = findActiveTerm();
        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>(request.admissionIds());
        if (uniqueIds.isEmpty()) {
            throw new InvalidAdmissionRequestException("En az bir kabul kaydı seçilmelidir.");
        }

        Map<Long, Admission> admissionsById = admissionRepository.findAllById(uniqueIds)
                .stream()
                .collect(Collectors.toMap(Admission::getId, Function.identity()));

        for (Long admissionId : uniqueIds) {
            Admission admission = admissionsById.get(admissionId);
            if (admission == null) {
                throw new AdmissionNotFoundException(admissionId);
            }
            if (!admission.getDormitoryTerm().getId().equals(activeTerm.getId())) {
                throw new AdmissionAccessDeniedException(
                        "Yalnızca aktif döneme ait kabul kayıtları toplu olarak onaylanabilir."
                );
            }
            validateStatusTransition(admission, AdmissionStatus.APPROVED);
        }

        Map<Long, List<AppUser>> dormitoryAdmins = loadDormitoryAdmins(
                uniqueIds.stream().map(admissionsById::get).toList()
        );
        uniqueIds.forEach(id -> applyStatusChange(
                admissionsById.get(id), AdmissionStatus.APPROVED, adminEmail, dormitoryAdmins
        ));
        return new BulkApproveAdmissionsResponse(uniqueIds.size());
    }

    @Transactional
    public BulkApproveAdmissionsResponse approveAllPendingCurrentTermAdmissions(
            String adminEmail
    ) {
        requireGlobalAdmin(adminEmail);
        DormitoryTerm activeTerm = findActiveTerm();
        List<Admission> admissions = admissionRepository.findAllByTermIdAndStatus(
                activeTerm.getId(), AdmissionStatus.PENDING
        );
        Map<Long, List<AppUser>> dormitoryAdmins = loadDormitoryAdmins(admissions);
        admissions.forEach(admission -> applyStatusChange(
                admission, AdmissionStatus.APPROVED, adminEmail, dormitoryAdmins
        ));
        return new BulkApproveAdmissionsResponse(admissions.size());
    }

    private void applyStatusChange(
            Admission admission,
            AdmissionStatus requestedStatus,
            String adminEmail,
            Map<Long, List<AppUser>> dormitoryAdmins
    ) {
        validateStatusTransition(admission, requestedStatus);
        admission.setStatus(requestedStatus);
        createAdmissionNotification(admission);

        if (requestedStatus == AdmissionStatus.APPROVED) {
            createDormitoryAdminNotifications(admission, dormitoryAdmins);
        }

        AppUser studentUser = admission.getStudent().getUser();
        String studentName = studentUser.getFirstName() + " " + studentUser.getLastName();
        auditLogService.recordStudentEvent(
                adminEmail,
                AuditCategory.STUDENT_ACTIVITY,
                requestedStatus == AdmissionStatus.APPROVED
                        ? AuditAction.ADMISSION_APPROVED : AuditAction.ADMISSION_REJECTED,
                AuditEntityType.ADMISSION,
                admission.getId(), studentName, admission.getStudent(),
                admission.getDormitory(),
                studentName + " öğrencisinin kabulü "
                        + (requestedStatus == AdmissionStatus.APPROVED ? "onaylandı." : "reddedildi.")
        );
    }

    private Map<Long, List<AppUser>> loadDormitoryAdmins(List<Admission> admissions) {
        Set<Long> dormitoryIds = admissions.stream()
                .filter(admission -> admission.getDormitoryTerm().isActive())
                .map(admission -> admission.getDormitory().getId())
                .collect(Collectors.toSet());
        if (dormitoryIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return appUserRepository.findActiveDormitoryAdminsByDormitoryIds(dormitoryIds)
                .stream()
                .collect(Collectors.groupingBy(admin -> admin.getDormitory().getId()));
    }

    private void createDormitoryAdminNotifications(
            Admission admission,
            Map<Long, List<AppUser>> dormitoryAdmins
    ) {
        AppUser studentUser = admission.getStudent().getUser();
        String studentName = studentUser.getFirstName() + " " + studentUser.getLastName();
        dormitoryAdmins.getOrDefault(admission.getDormitory().getId(), List.of())
                .forEach(admin -> notificationService.createNotification(
                        admin,
                        NotificationType.ADMISSION_APPROVED,
                        "Yeni öğrenci kaydı",
                        studentName + " yurdunuza öğrenci olarak kabul edildi.",
                        NotificationReferenceType.ADMISSION,
                        admission.getId()
                ));
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidAdmissionRequestException(
                    "Sayfa 0 veya daha büyük, sayfa boyutu 1 ile 100 arasında olmalıdır."
            );
        }
    }

    private DormitoryTerm findActiveTerm() {
        return dormitoryTermRepository.findByActiveTrue()
                .orElseThrow(ActiveDormitoryTermNotFoundException::new);
    }

    private AppUser requireGlobalAdmin(String adminEmail) {
        AppUser admin = findAuthenticatedAdmin(adminEmail);
        if (admin.getAdminScope() != AdminScope.GLOBAL) {
            throw new AccessDeniedException(
                    "Bu işlem yalnızca GLOBAL adminler tarafından yapılabilir."
            );
        }
        return admin;
    }

    private void validateStatusTransition(
            Admission admission,
            AdmissionStatus requestedStatus
    ) {
        AdmissionStatus currentStatus =
                admission.getStatus();

        boolean validTransition =
                currentStatus == AdmissionStatus.PENDING
                        && (requestedStatus == AdmissionStatus.APPROVED
                        || requestedStatus == AdmissionStatus.REJECTED);

        if (!validTransition) {
            throw new InvalidAdmissionStatusTransitionException(
                    admission.getId(),
                    currentStatus,
                    requestedStatus
            );
        }
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

    private void createAdmissionNotification(
            Admission admission
    ) {

        AdmissionStatus status =
                admission.getStatus();

        if (status != AdmissionStatus.APPROVED
                && status != AdmissionStatus.REJECTED) {
            return;
        }

        AppUser studentUser =
                admission
                        .getStudent()
                        .getUser();

        String title;

        String message;

        if (status == AdmissionStatus.APPROVED) {

            title =
                    "Yurt Kabulünüz Onaylandı";

            message =
                    "Yurt kabul başvurunuz onaylandı.";

        } else {

            title =
                    "Yurt Kabulünüz Reddedildi";

            message =
                    "Yurt kabul başvurunuz reddedildi.";
        }

        notificationService.createNotification(

                studentUser.getId(),

                status == AdmissionStatus.APPROVED
                        ? NotificationType.ADMISSION_APPROVED
                        : NotificationType.ADMISSION_REJECTED,

                title,

                message,

                NotificationReferenceType.ADMISSION,

                admission.getId()
        );
    }

}
