package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.CreateUserRequest;
import com.ibb.yurtlar.dto.UpdateUserRequest;
import com.ibb.yurtlar.dto.UserResponse;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.exception.DormitoryNotFoundException;
import com.ibb.yurtlar.exception.EmailAlreadyExistsException;
import com.ibb.yurtlar.exception.InactiveDormitoryException;
import com.ibb.yurtlar.exception.InvalidAdminConfigurationException;
import com.ibb.yurtlar.exception.UserNotFoundException;
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

    public AppUserService(
            AppUserRepository appUserRepository,
            DormitoryRepository dormitoryRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.dormitoryRepository = dormitoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(
            CreateUserRequest request
    ) {
        String normalizedEmail =
                normalizeEmail(request.email());

        if (appUserRepository
                .existsByEmailIgnoreCase(normalizedEmail)) {

            throw new EmailAlreadyExistsException(
                    normalizedEmail
            );
        }

        AppUser user = new AppUser();

        user.setFirstName(
                request.firstName().trim()
        );

        user.setLastName(
                request.lastName().trim()
        );

        user.setEmail(normalizedEmail);

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.password()
                )
        );

        user.setRole(request.role());
        user.setActive(request.active());

        applyAdminConfiguration(
                user,
                request.role(),
                request.adminScope(),
                request.dormitoryId()
        );

        AppUser savedUser =
                appUserRepository.save(user);

        return toResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return appUserRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return toResponse(
                findUserById(id)
        );
    }

    @Transactional
    public UserResponse update(
            Long id,
            UpdateUserRequest request
    ) {
        AppUser user =
                findUserById(id);

        String normalizedEmail =
                normalizeEmail(request.email());

        boolean anotherUserUsesEmail =
                appUserRepository
                        .existsByEmailIgnoreCaseAndIdNot(
                                normalizedEmail,
                                id
                        );

        if (anotherUserUsesEmail) {
            throw new EmailAlreadyExistsException(
                    normalizedEmail
            );
        }

        user.setFirstName(
                request.firstName().trim()
        );

        user.setLastName(
                request.lastName().trim()
        );

        user.setEmail(normalizedEmail);
        user.setRole(request.role());
        user.setActive(request.active());

        applyAdminConfiguration(
                user,
                request.role(),
                request.adminScope(),
                request.dormitoryId()
        );

        return toResponse(user);
    }

    private void applyAdminConfiguration(
            AppUser user,
            Role role,
            AdminScope adminScope,
            Long dormitoryId
    ) {
        if (role != Role.ADMIN) {
            validateNonAdminConfiguration(
                    adminScope,
                    dormitoryId
            );

            user.setAdminScope(null);
            user.setDormitory(null);

            return;
        }

        if (adminScope == null) {
            throw new InvalidAdminConfigurationException(
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

    private void validateNonAdminConfiguration(
            AdminScope adminScope,
            Long dormitoryId
    ) {
        if (adminScope != null || dormitoryId != null) {
            throw new InvalidAdminConfigurationException(
                    "STUDENT ve REVIEWER kullanıcıları için adminScope ve dormitoryId gönderilemez."
            );
        }
    }

    private void configureGlobalAdmin(
            AppUser user,
            Long dormitoryId
    ) {
        if (dormitoryId != null) {
            throw new InvalidAdminConfigurationException(
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
            throw new InvalidAdminConfigurationException(
                    "DORMITORY kapsamındaki admin için dormitoryId zorunludur."
            );
        }

        Dormitory dormitory =
                dormitoryRepository
                        .findById(dormitoryId)
                        .orElseThrow(
                                () -> new DormitoryNotFoundException(
                                        dormitoryId
                                )
                        );

        if (!dormitory.isActive()) {
            throw new InactiveDormitoryException(
                    dormitoryId
            );
        }

        user.setAdminScope(
                AdminScope.DORMITORY
        );

        user.setDormitory(dormitory);
    }

    private AppUser findUserById(Long id) {
        return appUserRepository
                .findById(id)
                .orElseThrow(
                        () -> new UserNotFoundException(id)
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
}