package com.ibb.yurtlar.service;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.*;

import com.ibb.yurtlar.exception.BusinessException;

import com.ibb.yurtlar.dto.CreateDormitoryRequest;
import com.ibb.yurtlar.dto.DormitoryResponse;
import com.ibb.yurtlar.dto.UpdateDormitoryRequest;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.DormitoryRepository;
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditEntityType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DormitoryService {

    private final DormitoryRepository dormitoryRepository;
    private final AppUserRepository appUserRepository;
    private final AuditLogService auditLogService;

    public DormitoryService(
            DormitoryRepository dormitoryRepository,
            AppUserRepository appUserRepository,
            AuditLogService auditLogService
    ) {
        this.dormitoryRepository = dormitoryRepository;
        this.appUserRepository = appUserRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public DormitoryResponse create(
            CreateDormitoryRequest request,
            String adminEmail
    ) {
        validateGlobalAdmin(adminEmail);

        String normalizedName =
                normalizeRequiredText(request.name());

        if (dormitoryRepository
                .existsByNameIgnoreCase(normalizedName)) {

            throw new BusinessException(DORMITORY_ALREADY_EXISTS,
                    normalizedName
            );
        }

        Dormitory dormitory = new Dormitory();

        dormitory.setName(normalizedName);
        dormitory.setAddress(
                normalizeOptionalText(request.address())
        );
        dormitory.setCapacity(request.capacity());
        dormitory.setActive(true);

        Dormitory savedDormitory =
                dormitoryRepository.save(dormitory);

        auditLogService.recordSystemEvent(adminEmail, AuditAction.DORMITORY_CREATED,
                AuditEntityType.DORMITORY, savedDormitory.getId(), savedDormitory.getName(),
                savedDormitory, savedDormitory.getName() + " yurdu oluşturuldu.");

        return toResponse(savedDormitory);
    }

    @Transactional(readOnly = true)
    public List<DormitoryResponse> getAll() {
        return dormitoryRepository
                .findAllByOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DormitoryResponse> getAllActive() {
        return dormitoryRepository
                .findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DormitoryResponse getById(Long id) {
        return toResponse(
                findDormitoryById(id)
        );
    }

    @Transactional
    public DormitoryResponse update(
            Long id,
            UpdateDormitoryRequest request,
            String adminEmail
    ) {
        validateGlobalAdmin(adminEmail);

        Dormitory dormitory =
                findDormitoryById(id);

        String normalizedName =
                normalizeRequiredText(request.name());

        boolean anotherDormitoryUsesName =
                dormitoryRepository
                        .existsByNameIgnoreCaseAndIdNot(
                                normalizedName,
                                id
                        );

        if (anotherDormitoryUsesName) {
            throw new BusinessException(DORMITORY_ALREADY_EXISTS,
                    normalizedName
            );
        }

        dormitory.setName(normalizedName);
        dormitory.setAddress(
                normalizeOptionalText(request.address())
        );
        dormitory.setCapacity(request.capacity());
        dormitory.setActive(request.active());

        auditLogService.recordSystemEvent(adminEmail, AuditAction.DORMITORY_UPDATED,
                AuditEntityType.DORMITORY, dormitory.getId(), dormitory.getName(),
                dormitory, dormitory.getName() + " yurdu güncellendi.");

        return toResponse(dormitory);
    }

    private Dormitory findDormitoryById(Long id) {
        return dormitoryRepository
                .findById(id)
                .orElseThrow(
                        () -> new BusinessException(DORMITORY_NOT_FOUND, id)
                );
    }

    private String normalizeRequiredText(
            String value
    ) {
        return value.trim();
    }

    private String normalizeOptionalText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();

        return trimmedValue.isEmpty()
                ? null
                : trimmedValue;
    }

    private DormitoryResponse toResponse(
            Dormitory dormitory
    ) {
        return new DormitoryResponse(
                dormitory.getId(),
                dormitory.getName(),
                dormitory.getAddress(),
                dormitory.getCapacity(),
                dormitory.isActive(),
                dormitory.getCreatedAt(),
                dormitory.getUpdatedAt()
        );
    }

    private void validateGlobalAdmin(String email) {
        AppUser admin = appUserRepository
                .findByNormalizedEmail(email)
                .orElseThrow(() -> new BusinessException(INVALID_CREDENTIALS));

        if (admin.getRole() != Role.ADMIN) {
            throw new BusinessException(USER_IS_NOT_ADMIN, admin.getId());
        }

        if (admin.getAdminScope() != AdminScope.GLOBAL) {
            throw new BusinessException(USER_MANAGEMENT_ACCESS_DENIED,
                    "Bu işlem yalnızca GLOBAL adminler tarafından yapılabilir."
            );
        }
    }
}
