package com.ibb.yurtlar.service;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.*;

import com.ibb.yurtlar.exception.BusinessException;

import com.ibb.yurtlar.dto.CurrentUserResponse;
import com.ibb.yurtlar.dto.LoginRequest;
import com.ibb.yurtlar.dto.LoginResponse;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final AppUserRepository
            appUserRepository;

    private final PasswordEncoder
            passwordEncoder;

    private final JwtService
            jwtService;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.appUserRepository =
                appUserRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.jwtService =
                jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(
            LoginRequest request
    ) {
        String normalizedEmail =
                normalizeEmail(request.email());

        AppUser user =
                appUserRepository
                        .findByNormalizedEmail(normalizedEmail)
                        .orElseThrow(() -> {
                            return new BusinessException(INVALID_CREDENTIALS);
                        });

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.password(),
                        user.getPasswordHash()
                );

        if (!passwordMatches) {
            throw new BusinessException(INVALID_CREDENTIALS);
        }

        if (!user.isActive()) {
            throw new BusinessException(INVALID_CREDENTIALS);
        }

        String accessToken =
                jwtService.generateToken(user);

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

        return new LoginResponse(
                accessToken,
                "Bearer",

                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),

                user.getRole(),
                user.getAdminScope(),

                dormitoryId,
                dormitoryName
        );
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(
            String authenticatedEmail
    ) {
        AppUser user =
                appUserRepository
                        .findByNormalizedEmail(
                                authenticatedEmail
                        )
                        .orElseThrow(
                                () -> new BusinessException(INVALID_CREDENTIALS)
                        );

        if (!user.isActive()) {
            throw new BusinessException(INVALID_CREDENTIALS);
        }

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

        return new CurrentUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),

                user.getRole(),
                user.getAdminScope(),
                user.isActive(),

                dormitoryId,
                dormitoryName
        );
    }

    private String normalizeEmail(

            String email
    ) {
        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }




}
