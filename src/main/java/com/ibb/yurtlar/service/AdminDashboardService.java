package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.AdminDashboardResponse;
import com.ibb.yurtlar.dto.AdminRecentAdmissionResponse;
import com.ibb.yurtlar.dto.DormitoryAdminDashboardResponse;
import com.ibb.yurtlar.entity.Admission;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.entity.Student;
import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.AdmissionStatus;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.enums.StudentDocumentStatus;
import com.ibb.yurtlar.enums.DormitoryAdmissionProcessStatus;
import com.ibb.yurtlar.exception.ActiveDormitoryTermNotFoundException;
import com.ibb.yurtlar.exception.InvalidAdminConfigurationException;
import com.ibb.yurtlar.exception.UserIsNotAdminException;
import com.ibb.yurtlar.exception.UserNotFoundException;
import com.ibb.yurtlar.repository.AdmissionRepository;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.DormitoryTermRepository;
import com.ibb.yurtlar.repository.StudentDocumentRepository;
import com.ibb.yurtlar.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.ibb.yurtlar.dto.AdmissionStatusCountResponse;
import com.ibb.yurtlar.dto.DocumentStatusCountResponse;
import com.ibb.yurtlar.dto.DormitoryAdminDashboardResponse;
import com.ibb.yurtlar.dto.DormitoryReviewerWorkloadResponse;
import com.ibb.yurtlar.dto.DormitoryStudentProgressResponse;
import com.ibb.yurtlar.dto.PendingDocumentTypeCountResponse;
import com.ibb.yurtlar.repository.DocumentReviewRepository;
import com.ibb.yurtlar.dto.GlobalAdminDashboardResponse;
import com.ibb.yurtlar.dto.GlobalDormitoryStatisticsResponse;


import java.util.List;

@Service
public class AdminDashboardService {

    private final DormitoryTermRepository dormitoryTermRepository;
    private final AppUserRepository appUserRepository;
    private final StudentRepository studentRepository;
    private final AdmissionRepository admissionRepository;
    private final StudentDocumentRepository studentDocumentRepository;
    private final DocumentReviewRepository
            documentReviewRepository;

    private final DormitoryStudentProgressService
            dormitoryStudentProgressService;

    private final GlobalDormitoryStatisticsService
            globalDormitoryStatisticsService;

    public AdminDashboardService(
            DormitoryTermRepository dormitoryTermRepository,
            AppUserRepository appUserRepository,
            StudentRepository studentRepository,
            AdmissionRepository admissionRepository,
            StudentDocumentRepository studentDocumentRepository,
            DocumentReviewRepository documentReviewRepository,
            DormitoryStudentProgressService
                    dormitoryStudentProgressService,
            GlobalDormitoryStatisticsService
                    globalDormitoryStatisticsService
    ) {
        this.dormitoryTermRepository = dormitoryTermRepository;
        this.appUserRepository = appUserRepository;
        this.studentRepository = studentRepository;
        this.admissionRepository = admissionRepository;
        this.studentDocumentRepository = studentDocumentRepository;
        this.documentReviewRepository =
                documentReviewRepository;

        this.dormitoryStudentProgressService =
                dormitoryStudentProgressService;
        this.globalDormitoryStatisticsService = globalDormitoryStatisticsService;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard(
            Long adminUserId
    ) {
        AppUser admin =
                findAdminById(adminUserId);

        DormitoryTerm activeTerm =
                dormitoryTermRepository
                        .findByActiveTrue()
                        .orElseThrow(
                                ActiveDormitoryTermNotFoundException::new
                        );

        if (admin.getAdminScope() == AdminScope.GLOBAL) {
            return createGlobalDashboard(
                    admin,
                    activeTerm
            );
        }

        if (admin.getAdminScope() == AdminScope.DORMITORY) {
            return createDormitoryDashboard(
                    admin,
                    activeTerm
            );
        }

        throw new InvalidAdminConfigurationException(
                "ADMIN kullanıcısının adminScope bilgisi bulunmamaktadır."
        );
    }

    private AdminDashboardResponse createGlobalDashboard(
            AppUser admin,
            DormitoryTerm activeTerm
    ) {
        Long activeTermId =
                activeTerm.getId();

        long totalUsers =
                appUserRepository.count();

        long adminUserCount =
                appUserRepository.countByRole(
                        Role.ADMIN
                );

        long reviewerUserCount =
                appUserRepository.countByRole(
                        Role.REVIEWER
                );

        long studentUserCount =
                appUserRepository.countByRole(
                        Role.STUDENT
                );

        long studentProfileCount =
                studentRepository.count();

        long totalAdmissions =
                admissionRepository
                        .countByDormitoryTerm_Id(
                                activeTermId
                        );

        long pendingAdmissions =
                admissionRepository
                        .countByDormitoryTerm_IdAndStatus(
                                activeTermId,
                                AdmissionStatus.PENDING
                        );

        long approvedAdmissions =
                admissionRepository
                        .countByDormitoryTerm_IdAndStatus(
                                activeTermId,
                                AdmissionStatus.APPROVED
                        );

        long rejectedAdmissions =
                admissionRepository
                        .countByDormitoryTerm_IdAndStatus(
                                activeTermId,
                                AdmissionStatus.REJECTED
                        );

        long pendingDocumentCount =
                studentDocumentRepository
                        .countByTermAndStatus(
                                activeTermId,
                                StudentDocumentStatus.UPLOADED
                        );

        long approvedDocumentCount =
                studentDocumentRepository
                        .countByTermAndStatus(
                                activeTermId,
                                StudentDocumentStatus.APPROVED
                        );

        long rejectedDocumentCount =
                studentDocumentRepository
                        .countByTermAndStatus(
                                activeTermId,
                                StudentDocumentStatus.REJECTED
                        );

        long revisionRequiredDocumentCount =
                studentDocumentRepository
                        .countByTermAndStatus(
                                activeTermId,
                                StudentDocumentStatus.REVISION_REQUIRED
                        );

        List<AdminRecentAdmissionResponse> recentAdmissions =
                admissionRepository
                        .findTop10ByDormitoryTerm_IdOrderByCreatedAtDesc(
                                activeTermId
                        )
                        .stream()
                        .map(this::toRecentAdmissionResponse)
                        .toList();

        return new AdminDashboardResponse(
                admin.getId(),
                admin.getFirstName(),
                admin.getLastName(),

                admin.getAdminScope(),

                null,
                null,

                activeTerm.getId(),
                activeTerm.getName(),

                totalUsers,
                adminUserCount,
                reviewerUserCount,
                studentUserCount,
                studentProfileCount,

                totalAdmissions,
                pendingAdmissions,
                approvedAdmissions,
                rejectedAdmissions,

                pendingDocumentCount,
                approvedDocumentCount,
                rejectedDocumentCount,
                revisionRequiredDocumentCount,

                recentAdmissions
        );
    }

    private AdminDashboardResponse createDormitoryDashboard(
            AppUser admin,
            DormitoryTerm activeTerm
    ) {
        Dormitory dormitory =
                admin.getDormitory();

        if (dormitory == null) {
            throw new InvalidAdminConfigurationException(
                    "DORMITORY kapsamındaki adminin bağlı olduğu yurt bulunmamaktadır."
            );
        }

        Long activeTermId =
                activeTerm.getId();

        Long dormitoryId =
                dormitory.getId();

        long totalAdmissions =
                admissionRepository
                        .countByDormitoryTerm_IdAndDormitory_Id(
                                activeTermId,
                                dormitoryId
                        );

        long pendingAdmissions =
                admissionRepository
                        .countByDormitoryTerm_IdAndDormitory_IdAndStatus(
                                activeTermId,
                                dormitoryId,
                                AdmissionStatus.PENDING
                        );

        long approvedAdmissions =
                admissionRepository
                        .countByDormitoryTerm_IdAndDormitory_IdAndStatus(
                                activeTermId,
                                dormitoryId,
                                AdmissionStatus.APPROVED
                        );

        long rejectedAdmissions =
                admissionRepository
                        .countByDormitoryTerm_IdAndDormitory_IdAndStatus(
                                activeTermId,
                                dormitoryId,
                                AdmissionStatus.REJECTED
                        );

        long pendingDocumentCount =
                studentDocumentRepository
                        .countByTermAndDormitoryAndStatus(
                                activeTermId,
                                dormitoryId,
                                StudentDocumentStatus.UPLOADED
                        );

        long approvedDocumentCount =
                studentDocumentRepository
                        .countByTermAndDormitoryAndStatus(
                                activeTermId,
                                dormitoryId,
                                StudentDocumentStatus.APPROVED
                        );

        long rejectedDocumentCount =
                studentDocumentRepository
                        .countByTermAndDormitoryAndStatus(
                                activeTermId,
                                dormitoryId,
                                StudentDocumentStatus.REJECTED
                        );

        long revisionRequiredDocumentCount =
                studentDocumentRepository
                        .countByTermAndDormitoryAndStatus(
                                activeTermId,
                                dormitoryId,
                                StudentDocumentStatus.REVISION_REQUIRED
                        );

        List<AdminRecentAdmissionResponse> recentAdmissions =
                admissionRepository
                        .findTop10ByDormitoryTerm_IdAndDormitory_IdOrderByCreatedAtDesc(
                                activeTermId,
                                dormitoryId
                        )
                        .stream()
                        .map(this::toRecentAdmissionResponse)
                        .toList();

        /*
         * Yurt adminine genel kullanıcı hesaplarının tamamını göstermiyoruz.
         * Şimdilik kullanıcı sayaçlarında yalnızca yurda ait admission/student
         * bilgisini temsil eden değerleri kullanıyoruz.
         *
         * JWT ve ekran ihtiyaçları netleştiğinde bu alanları daha özel
         * yurt sorgularıyla genişletebiliriz.
         */
        long totalUsers =
                totalAdmissions;

        long adminUserCount =
                1;

        long reviewerUserCount =
                0;

        long studentUserCount =
                totalAdmissions;

        long studentProfileCount =
                totalAdmissions;

        return new AdminDashboardResponse(
                admin.getId(),
                admin.getFirstName(),
                admin.getLastName(),

                admin.getAdminScope(),

                dormitory.getId(),
                dormitory.getName(),

                activeTerm.getId(),
                activeTerm.getName(),

                totalUsers,
                adminUserCount,
                reviewerUserCount,
                studentUserCount,
                studentProfileCount,

                totalAdmissions,
                pendingAdmissions,
                approvedAdmissions,
                rejectedAdmissions,

                pendingDocumentCount,
                approvedDocumentCount,
                rejectedDocumentCount,
                revisionRequiredDocumentCount,

                recentAdmissions
        );
    }

    private AppUser findAdminById(
            Long adminUserId
    ) {
        AppUser admin =
                appUserRepository
                        .findById(adminUserId)
                        .orElseThrow(
                                () -> new UserNotFoundException(
                                        adminUserId
                                )
                        );

        if (admin.getRole() != Role.ADMIN) {
            throw new UserIsNotAdminException(
                    adminUserId
            );
        }

        return admin;
    }

    private AdminRecentAdmissionResponse toRecentAdmissionResponse(
            Admission admission
    ) {
        Student student =
                admission.getStudent();

        AppUser studentUser =
                student.getUser();

        Dormitory dormitory =
                admission.getDormitory();

        return new AdminRecentAdmissionResponse(
                admission.getId(),

                student.getId(),
                studentUser.getFirstName(),
                studentUser.getLastName(),
                student.getIdentityNumber(),

                dormitory.getName(),

                admission.getStatus(),
                admission.getAdmissionDate(),
                admission.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getMyDashboard(
            String email
    ) {
        AppUser admin =
                appUserRepository
                        .findByNormalizedEmail(email)
                        .orElseThrow(
                                () -> new UsernameNotFoundException(
                                        "Giriş yapan kullanıcı bulunamadı."
                                )
                        );

        if (admin.getRole() != Role.ADMIN) {
            throw new UserIsNotAdminException(
                    admin.getId()
            );
        }

        return getDashboard(
                admin.getId()
        );
    }

    @Transactional(readOnly = true)
    public DormitoryAdminDashboardResponse
    getMyDormitoryDashboard(
            String email
    ) {
        AppUser admin =
                appUserRepository
                        .findByNormalizedEmail(
                                email
                        )
                        .orElseThrow(
                                () -> new UsernameNotFoundException(
                                        "Giriş yapan kullanıcı bulunamadı."
                                )
                        );

        if (admin.getRole() != Role.ADMIN) {
            throw new UserIsNotAdminException(
                    admin.getId()
            );
        }

        if (admin.getAdminScope()
                != AdminScope.DORMITORY) {

            throw new InvalidAdminConfigurationException(
                    "Bu dashboard yalnızca yurt adminleri "
                            + "tarafından kullanılabilir."
            );
        }

        Dormitory dormitory =
                admin.getDormitory();

        if (dormitory == null) {
            throw new InvalidAdminConfigurationException(
                    "Yurt adminine bir yurt atanmamıştır."
            );
        }

        DormitoryTerm activeTerm =
                dormitoryTermRepository
                        .findByActiveTrue()
                        .orElseThrow(
                                ActiveDormitoryTermNotFoundException::new
                        );

        Long dormitoryId =
                dormitory.getId();

        Long activeTermId =
                activeTerm.getId();

        DormitoryStudentProgressResponse progress =
                dormitoryStudentProgressService
                        .calculateProgress(
                                dormitoryId,
                                activeTermId
                        );

        AdmissionStatusCountResponse admissionCounts =
                admissionRepository
                        .getStatusCountsForDormitory(
                                activeTermId,
                                dormitoryId
                        );

        DocumentStatusCountResponse documentCounts =
                studentDocumentRepository
                        .getStatusCountsForDormitory(
                                activeTermId,
                                dormitoryId
                        );

        long totalReviewerCount =
                appUserRepository
                        .countByRoleAndDormitoryAndOptionalActive(
                                Role.REVIEWER,
                                dormitoryId,
                                null
                        );

        long activeReviewerCount =
                appUserRepository
                        .countByRoleAndDormitoryAndOptionalActive(
                                Role.REVIEWER,
                                dormitoryId,
                                true
                        );

        long inactiveReviewerCount =
                totalReviewerCount
                        - activeReviewerCount;

        List<DormitoryReviewerWorkloadResponse>
                reviewerWorkloads =
                documentReviewRepository
                        .findReviewerWorkloads(
                                activeTermId,
                                dormitoryId
                        );

        List<PendingDocumentTypeCountResponse>
                pendingDocumentsByType =
                studentDocumentRepository
                        .findPendingDocumentCountsByType(
                                dormitoryId,
                                StudentDocumentStatus.UPLOADED
                        );

        int capacity =
                dormitory.getCapacity();

        long activeStudentCount =
                progress.activeStudentCount();

        long availableCapacity =
                Math.max(
                        capacity - activeStudentCount,
                        0
                );

        int occupancyPercentage =
                calculateOccupancyPercentage(
                        capacity,
                        activeStudentCount
                );

        return new DormitoryAdminDashboardResponse(
                admin.getId(),
                admin.getFirstName(),
                admin.getLastName(),
                admin.getEmail(),

                dormitory.getId(),
                dormitory.getName(),

                activeTerm.getId(),
                activeTerm.getName(),

                capacity,
                activeStudentCount,
                availableCapacity,
                occupancyPercentage,

                admissionCounts.totalCount(),
                admissionCounts.pendingCount(),
                admissionCounts.approvedCount(),
                admissionCounts.rejectedCount(),

                progress.completedStudentCount(),
                progress.incompleteStudentCount(),
                progress.studentCompletionPercentage(),

                totalReviewerCount,
                activeReviewerCount,
                inactiveReviewerCount,

                documentCounts.pendingCount(),
                documentCounts.approvedCount(),
                documentCounts.rejectedCount(),
                documentCounts.revisionRequiredCount(),

                reviewerWorkloads,
                pendingDocumentsByType,
                progress.actionRequiredStudents()
        );
    }

    @Transactional(readOnly = true)
    public GlobalAdminDashboardResponse
    getMyGlobalDashboard(
            String email
    ) {
        AppUser admin =
                appUserRepository
                        .findByNormalizedEmail(
                                email
                        )
                        .orElseThrow(
                                () -> new UsernameNotFoundException(
                                        "Giriş yapan kullanıcı bulunamadı."
                                )
                        );

        if (admin.getRole() != Role.ADMIN) {
            throw new UserIsNotAdminException(
                    admin.getId()
            );
        }

        if (admin.getAdminScope()
                != AdminScope.GLOBAL) {

            throw new InvalidAdminConfigurationException(
                    "Bu dashboard yalnızca GLOBAL adminler "
                            + "tarafından kullanılabilir."
            );
        }

        DormitoryTerm activeTerm =
                dormitoryTermRepository
                        .findByActiveTrue()
                        .orElseThrow(
                                ActiveDormitoryTermNotFoundException::new
                        );

        Long activeTermId =
                activeTerm.getId();

        List<GlobalDormitoryStatisticsResponse>
                dormitoryStatistics =
                globalDormitoryStatisticsService
                        .createStatistics(
                                activeTermId
                        );

        long totalDormitoryCount =
                dormitoryStatistics.size();

        long activeDormitoryCount =
                dormitoryStatistics
                        .stream()
                        .filter(
                                GlobalDormitoryStatisticsResponse
                                        ::active
                        )
                        .count();

        long inactiveDormitoryCount =
                totalDormitoryCount
                        - activeDormitoryCount;

        long totalCapacity =
                dormitoryStatistics
                        .stream()
                        .mapToLong(
                                GlobalDormitoryStatisticsResponse
                                        ::capacity
                        )
                        .sum();

        long activeStudentCount =
                dormitoryStatistics
                        .stream()
                        .mapToLong(
                                GlobalDormitoryStatisticsResponse
                                        ::activeStudentCount
                        )
                        .sum();

        long availableCapacity =
                Math.max(
                        totalCapacity
                                - activeStudentCount,
                        0
                );

        int occupancyPercentage =
                calculateOccupancyPercentage(
                        totalCapacity,
                        activeStudentCount
                );

        AdmissionStatusCountResponse admissionCounts =
                admissionRepository
                        .getGlobalStatusCountsForActiveTerm(
                                activeTermId
                        );

        long completedStudentCount =
                dormitoryStatistics
                        .stream()
                        .mapToLong(
                                GlobalDormitoryStatisticsResponse
                                        ::completedStudentCount
                        )
                        .sum();

        long incompleteStudentCount =
                dormitoryStatistics
                        .stream()
                        .mapToLong(
                                GlobalDormitoryStatisticsResponse
                                        ::incompleteStudentCount
                        )
                        .sum();

        long actionRequiredStudentCount =
                dormitoryStatistics
                        .stream()
                        .mapToLong(
                                GlobalDormitoryStatisticsResponse
                                        ::actionRequiredStudentCount
                        )
                        .sum();

        int studentCompletionPercentage =
                calculateStudentCompletionPercentage(
                        activeStudentCount,
                        completedStudentCount
                );

        long totalAdminCount =
                appUserRepository
                        .countByRole(
                                Role.ADMIN
                        );

        long globalAdminCount =
                appUserRepository
                        .countAdminsByScope(
                                AdminScope.GLOBAL
                        );

        long dormitoryAdminCount =
                appUserRepository
                        .countAdminsByScope(
                                AdminScope.DORMITORY
                        );

        long totalReviewerCount =
                appUserRepository
                        .countReviewersByOptionalActive(
                                null
                        );

        long activeReviewerCount =
                appUserRepository
                        .countReviewersByOptionalActive(
                                true
                        );

        long inactiveReviewerCount =
                totalReviewerCount
                        - activeReviewerCount;

        long totalStudentUserCount =
                appUserRepository
                        .countByRole(
                                Role.STUDENT
                        );

        DocumentStatusCountResponse documentCounts =
                studentDocumentRepository
                        .getGlobalStatusCountsForActiveTerm(
                                activeTermId
                        );

        long admissionCompletedDormitoryCount =
                dormitoryStatistics
                        .stream()
                        .filter(statistics ->
                                statistics
                                        .admissionProcessStatus()
                                        == DormitoryAdmissionProcessStatus
                                        .COMPLETED
                        )
                        .count();

        long admissionInProgressDormitoryCount =
                dormitoryStatistics
                        .stream()
                        .filter(statistics ->
                                statistics
                                        .admissionProcessStatus()
                                        == DormitoryAdmissionProcessStatus
                                        .IN_PROGRESS
                        )
                        .count();

        return new GlobalAdminDashboardResponse(
                admin.getId(),
                admin.getFirstName(),
                admin.getLastName(),
                admin.getEmail(),

                activeTerm.getId(),
                activeTerm.getName(),

                totalDormitoryCount,
                activeDormitoryCount,
                inactiveDormitoryCount,

                totalCapacity,
                activeStudentCount,
                availableCapacity,
                occupancyPercentage,

                admissionCounts.totalCount(),
                admissionCounts.pendingCount(),
                admissionCounts.approvedCount(),
                admissionCounts.rejectedCount(),

                completedStudentCount,
                incompleteStudentCount,
                actionRequiredStudentCount,
                studentCompletionPercentage,

                totalAdminCount,
                globalAdminCount,
                dormitoryAdminCount,

                totalReviewerCount,
                activeReviewerCount,
                inactiveReviewerCount,

                totalStudentUserCount,

                documentCounts.pendingCount(),
                documentCounts.approvedCount(),
                documentCounts.rejectedCount(),
                documentCounts.revisionRequiredCount(),

                admissionCompletedDormitoryCount,
                admissionInProgressDormitoryCount,

                dormitoryStatistics
        );
    }

    private int calculateOccupancyPercentage(
            int capacity,
            long activeStudentCount
    ) {
        if (capacity <= 0) {
            return 0;
        }

        return (int) Math.round(
                activeStudentCount
                        * 100.0
                        / capacity
        );
    }

    private int calculateOccupancyPercentage(
            long totalCapacity,
            long activeStudentCount
    ) {
        if (totalCapacity <= 0) {
            return 0;
        }

        return (int) Math.round(
                activeStudentCount
                        * 100.0
                        / totalCapacity
        );
    }

    private int calculateStudentCompletionPercentage(
            long activeStudentCount,
            long completedStudentCount
    ) {
        if (activeStudentCount == 0) {
            return 0;
        }

        return (int) Math.round(
                completedStudentCount
                        * 100.0
                        / activeStudentCount
        );
    }
}