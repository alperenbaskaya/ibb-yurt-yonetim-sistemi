package com.ibb.yurtlar.service;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.*;

import com.ibb.yurtlar.exception.BusinessException;

import com.ibb.yurtlar.dto.CreateUserRequest;
import com.ibb.yurtlar.dto.UpdateUserRequest;
import com.ibb.yurtlar.dto.UserResponse;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditEntityType;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.DormitoryRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final DormitoryRepository dormitoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public AppUserService(
            AppUserRepository appUserRepository,
            DormitoryRepository dormitoryRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService
    ) {
        this.appUserRepository =
                appUserRepository;

        this.dormitoryRepository =
                dormitoryRepository;

        this.passwordEncoder =
                passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public UserResponse create(
            CreateUserRequest request,
            String adminEmail
    ) {
        AppUser admin =
                findAuthenticatedAdmin(
                        adminEmail
                );

        validateCreatePermission(
                admin,
                request
        );

        String normalizedEmail =
                normalizeEmail(
                        request.email()
                );

        if (appUserRepository
                .existsByEmailIgnoreCase(
                        normalizedEmail
                )) {

            throw new BusinessException(EMAIL_ALREADY_EXISTS,
                    normalizedEmail
            );
        }

        AppUser user =
                new AppUser();

        user.setFirstName(
                request.firstName().trim()
        );

        user.setLastName(
                request.lastName().trim()
        );

        user.setEmail(
                normalizedEmail
        );

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.password()
                )
        );

        user.setRole(
                request.role()
        );

        user.setActive(
                request.active()
        );

        applyRoleConfiguration(
                user,
                request.role(),
                request.adminScope(),
                request.dormitoryId()
        );

        AppUser savedUser =
                appUserRepository.save(
                        user
                );

        String savedName = savedUser.getFirstName() + " " + savedUser.getLastName();
        auditLogService.recordSystemEvent(adminEmail, AuditAction.USER_CREATED,
                AuditEntityType.USER, savedUser.getId(), savedName,
                savedUser.getDormitory(), savedName + " kullanıcısı oluşturuldu.");

        return toResponse(
                savedUser
        );
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAll(
            String adminEmail
    ) {
        AppUser admin =
                findAuthenticatedAdmin(
                        adminEmail
                );

        if (admin.getAdminScope()
                == AdminScope.GLOBAL) {

            return appUserRepository
                    .findAll()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        Dormitory adminDormitory =
                getDormitoryAdminDormitory(
                        admin
                );

        return appUserRepository
                .findByRoleAndDormitory(
                        Role.REVIEWER,
                        adminDormitory.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getById(
            Long id,
            String adminEmail
    ) {
        AppUser admin =
                findAuthenticatedAdmin(
                        adminEmail
                );

        AppUser targetUser =
                findUserById(
                        id
                );

        validateTargetUserAccess(
                admin,
                targetUser
        );

        return toResponse(
                targetUser
        );
    }

    @Transactional
    public UserResponse update(
            Long id,
            UpdateUserRequest request,
            String adminEmail
    ) {
        AppUser admin =
                findAuthenticatedAdmin(
                        adminEmail
                );

        AppUser user =
                findUserById(
                        id
                );
        boolean previouslyActive = user.isActive();

        validateTargetUserAccess(
                admin,
                user
        );

        validateUpdatePermission(
                admin,
                request
        );

        String normalizedEmail =
                normalizeEmail(
                        request.email()
                );

        boolean anotherUserUsesEmail =
                appUserRepository
                        .existsByEmailIgnoreCaseAndIdNot(
                                normalizedEmail,
                                id
                        );

        if (anotherUserUsesEmail) {
            throw new BusinessException(EMAIL_ALREADY_EXISTS,
                    normalizedEmail
            );
        }

        user.setFirstName(
                request.firstName().trim()
        );

        user.setLastName(
                request.lastName().trim()
        );

        user.setEmail(
                normalizedEmail
        );

        user.setRole(
                request.role()
        );

        user.setActive(
                request.active()
        );

        applyRoleConfiguration(
                user,
                request.role(),
                request.adminScope(),
                request.dormitoryId()
        );

        String userName = user.getFirstName() + " " + user.getLastName();
        auditLogService.recordSystemEvent(adminEmail, AuditAction.USER_UPDATED,
                AuditEntityType.USER, user.getId(), userName, user.getDormitory(),
                userName + " kullanıcısı güncellendi.");
        if (previouslyActive != user.isActive()) {
            AuditAction action = user.isActive()
                    ? AuditAction.USER_ACTIVATED : AuditAction.USER_DEACTIVATED;
            auditLogService.recordSystemEvent(adminEmail, action,
                    AuditEntityType.USER, user.getId(), userName, user.getDormitory(),
                    userName + (user.isActive()
                            ? " kullanıcısı aktif hale getirildi."
                            : " kullanıcısı pasif hale getirildi."));
        }

        return toResponse(
                user
        );
    }

    private void applyRoleConfiguration(
            AppUser user,
            Role role,
            AdminScope adminScope,
            Long dormitoryId
    ) {
        switch (role) {
            case ADMIN ->
                    configureAdmin(
                            user,
                            adminScope,
                            dormitoryId
                    );

            case REVIEWER ->
                    configureReviewer(
                            user,
                            adminScope,
                            dormitoryId
                    );

            case STUDENT ->
                    configureStudent(
                            user,
                            adminScope,
                            dormitoryId
                    );
        }
    }

    private void configureAdmin(
            AppUser user,
            AdminScope adminScope,
            Long dormitoryId
    ) {
        if (adminScope == null) {
            throw new BusinessException(INVALID_ADMIN_CONFIGURATION,
                    "ADMIN rolündeki kullanıcılar için adminScope zorunludur."
            );
        }

        if (adminScope == AdminScope.GLOBAL) {
            configureGlobalAdmin(
                    user,
                    dormitoryId
            );

            return;
        }

        configureDormitoryAdmin(
                user,
                dormitoryId
        );
    }

    private void configureGlobalAdmin(
            AppUser user,
            Long dormitoryId
    ) {
        if (dormitoryId != null) {
            throw new BusinessException(INVALID_ADMIN_CONFIGURATION,
                    "GLOBAL admin belirli bir yurda atanamaz. dormitoryId boş olmalıdır."
            );
        }

        user.setAdminScope(
                AdminScope.GLOBAL
        );

        user.setDormitory(null);
    }

    private void configureDormitoryAdmin(
            AppUser user,
            Long dormitoryId
    ) {
        if (dormitoryId == null) {
            throw new BusinessException(INVALID_ADMIN_CONFIGURATION,
                    "DORMITORY kapsamındaki admin için dormitoryId zorunludur."
            );
        }

        Dormitory dormitory =
                findActiveDormitory(
                        dormitoryId
                );

        user.setAdminScope(
                AdminScope.DORMITORY
        );

        user.setDormitory(dormitory);
    }

    private void configureReviewer(
            AppUser user,
            AdminScope adminScope,
            Long dormitoryId
    ) {
        if (adminScope != null) {
            throw new BusinessException(INVALID_USER_CONFIGURATION,
                    "REVIEWER kullanıcısı için adminScope gönderilemez."
            );
        }

        if (dormitoryId == null) {
            throw new BusinessException(INVALID_USER_CONFIGURATION,
                    "REVIEWER kullanıcısı için dormitoryId zorunludur."
            );
        }

        Dormitory dormitory =
                findActiveDormitory(
                        dormitoryId
                );

        user.setAdminScope(null);
        user.setDormitory(dormitory);
    }

    private void configureStudent(
            AppUser user,
            AdminScope adminScope,
            Long dormitoryId
    ) {
        if (adminScope != null
                || dormitoryId != null) {

            throw new BusinessException(INVALID_USER_CONFIGURATION,
                    "STUDENT kullanıcısı için adminScope ve dormitoryId gönderilemez."
            );
        }

        user.setAdminScope(null);
        user.setDormitory(null);
    }

    private Dormitory findActiveDormitory(
            Long dormitoryId
    ) {
        Dormitory dormitory =
                dormitoryRepository
                        .findById(dormitoryId)
                        .orElseThrow(
                                () -> new BusinessException(DORMITORY_NOT_FOUND,
                                        dormitoryId
                                )
                        );

        if (!dormitory.isActive()) {
            throw new BusinessException(INACTIVE_DORMITORY,
                    dormitoryId
            );
        }

        return dormitory;
    }

    private AppUser findUserById(
            Long id
    ) {
        return appUserRepository
                .findById(id)
                .orElseThrow(
                        () -> new BusinessException(USER_NOT_FOUND,
                                id
                        )
                );
    }

    private String normalizeEmail(
            String email
    ) {
        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private UserResponse toResponse(
            AppUser user
    ) {
        Dormitory dormitory =
                user.getDormitory();

        Long dormitoryId =
                dormitory == null
                        ? null
                        : dormitory.getId();

        String dormitoryName =
                dormitory == null
                        ? null
                        : dormitory.getName();

        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),

                user.getAdminScope(),

                dormitoryId,
                dormitoryName
        );
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
                                () -> new BusinessException(INVALID_CREDENTIALS)
                        );

        if (admin.getRole() != Role.ADMIN) {
            throw new BusinessException(USER_IS_NOT_ADMIN,
                    admin.getId()
            );
        }

        if (!admin.isActive()) {
            throw new BusinessException(INVALID_CREDENTIALS);
        }

        return admin;
    }

    private Dormitory getDormitoryAdminDormitory(
            AppUser admin
    ) {
        if (admin.getAdminScope()
                != AdminScope.DORMITORY) {

            throw new BusinessException(INVALID_ADMIN_CONFIGURATION,
                    "Kullanıcı yurt admini değildir."
            );
        }

        Dormitory dormitory =
                admin.getDormitory();

        if (dormitory == null) {
            throw new BusinessException(INVALID_ADMIN_CONFIGURATION,
                    "Yurt admini için yurt ataması zorunludur."
            );
        }

        return dormitory;
    }

    private void validateCreatePermission(
            AppUser admin,
            CreateUserRequest request
    ) {
        if (admin.getAdminScope()
                == AdminScope.GLOBAL) {

            return;
        }

        Dormitory adminDormitory =
                getDormitoryAdminDormitory(
                        admin
                );

        if (request.role()
                != Role.REVIEWER) {

            throw new BusinessException(USER_MANAGEMENT_ACCESS_DENIED,
                    "Yurt admini yalnızca REVIEWER kullanıcısı oluşturabilir."
            );
        }

        if (request.adminScope() != null) {
            throw new BusinessException(USER_MANAGEMENT_ACCESS_DENIED,
                    "Reviewer kullanıcısı için adminScope gönderilemez."
            );
        }

        if (request.dormitoryId() == null
                || !adminDormitory
                .getId()
                .equals(
                        request.dormitoryId()
                )) {

            throw new BusinessException(USER_MANAGEMENT_ACCESS_DENIED,
                    "Yurt admini yalnızca kendi yurduna reviewer atayabilir."
            );
        }
    }

    private void validateUpdatePermission(
            AppUser admin,
            UpdateUserRequest request
    ) {
        if (admin.getAdminScope()
                == AdminScope.GLOBAL) {

            return;
        }

        Dormitory adminDormitory =
                getDormitoryAdminDormitory(
                        admin
                );

        if (request.role()
                != Role.REVIEWER) {

            throw new BusinessException(USER_MANAGEMENT_ACCESS_DENIED,
                    "Yurt admini kullanıcı rolünü REVIEWER dışında değiştiremez."
            );
        }

        if (request.adminScope() != null) {
            throw new BusinessException(USER_MANAGEMENT_ACCESS_DENIED,
                    "Reviewer kullanıcısı için adminScope gönderilemez."
            );
        }

        if (request.dormitoryId() == null
                || !adminDormitory
                .getId()
                .equals(
                        request.dormitoryId()
                )) {

            throw new BusinessException(USER_MANAGEMENT_ACCESS_DENIED,
                    "Yurt admini reviewer kullanıcısını başka yurda taşıyamaz."
            );
        }
    }

    private void validateTargetUserAccess(
            AppUser admin,
            AppUser targetUser
    ) {
        if (admin.getAdminScope()
                == AdminScope.GLOBAL) {

            return;
        }

        Dormitory adminDormitory =
                getDormitoryAdminDormitory(
                        admin
                );

        Dormitory targetDormitory =
                targetUser.getDormitory();

        boolean allowed =
                targetUser.getRole()
                        == Role.REVIEWER
                        && targetDormitory != null
                        && adminDormitory
                        .getId()
                        .equals(
                                targetDormitory.getId()
                        );

        if (!allowed) {
            throw new BusinessException(USER_MANAGEMENT_ACCESS_DENIED,
                    "Bu kullanıcıyı görüntüleme veya güncelleme yetkiniz bulunmamaktadır."
            );
        }
    }
}
