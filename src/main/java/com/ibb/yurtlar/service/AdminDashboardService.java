package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.AdminDashboardResponse;
import com.ibb.yurtlar.dto.AdminRecentAdmissionResponse;
import com.ibb.yurtlar.entity.Admission;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.entity.Student;
import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.AdmissionStatus;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.enums.StudentDocumentStatus;
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

import java.util.List;

@Service
public class AdminDashboardService {

    private final DormitoryTermRepository dormitoryTermRepository;
    private final AppUserRepository appUserRepository;
    private final StudentRepository studentRepository;
    private final AdmissionRepository admissionRepository;
    private final StudentDocumentRepository studentDocumentRepository;

    public AdminDashboardService(
            DormitoryTermRepository dormitoryTermRepository,
            AppUserRepository appUserRepository,
            StudentRepository studentRepository,
            AdmissionRepository admissionRepository,
            StudentDocumentRepository studentDocumentRepository
    ) {
        this.dormitoryTermRepository = dormitoryTermRepository;
        this.appUserRepository = appUserRepository;
        this.studentRepository = studentRepository;
        this.admissionRepository = admissionRepository;
        this.studentDocumentRepository = studentDocumentRepository;
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
                        .countByAdmission_DormitoryTerm_IdAndStatus(
                                activeTermId,
                                StudentDocumentStatus.UPLOADED
                        );

        long approvedDocumentCount =
                studentDocumentRepository
                        .countByAdmission_DormitoryTerm_IdAndStatus(
                                activeTermId,
                                StudentDocumentStatus.APPROVED
                        );

        long rejectedDocumentCount =
                studentDocumentRepository
                        .countByAdmission_DormitoryTerm_IdAndStatus(
                                activeTermId,
                                StudentDocumentStatus.REJECTED
                        );

        long revisionRequiredDocumentCount =
                studentDocumentRepository
                        .countByAdmission_DormitoryTerm_IdAndStatus(
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
                        .countByAdmission_DormitoryTerm_IdAndAdmission_Dormitory_IdAndStatus(
                                activeTermId,
                                dormitoryId,
                                StudentDocumentStatus.UPLOADED
                        );

        long approvedDocumentCount =
                studentDocumentRepository
                        .countByAdmission_DormitoryTerm_IdAndAdmission_Dormitory_IdAndStatus(
                                activeTermId,
                                dormitoryId,
                                StudentDocumentStatus.APPROVED
                        );

        long rejectedDocumentCount =
                studentDocumentRepository
                        .countByAdmission_DormitoryTerm_IdAndAdmission_Dormitory_IdAndStatus(
                                activeTermId,
                                dormitoryId,
                                StudentDocumentStatus.REJECTED
                        );

        long revisionRequiredDocumentCount =
                studentDocumentRepository
                        .countByAdmission_DormitoryTerm_IdAndAdmission_Dormitory_IdAndStatus(
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
}