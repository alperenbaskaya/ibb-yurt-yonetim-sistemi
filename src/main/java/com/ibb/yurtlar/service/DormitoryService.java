package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.CreateDormitoryRequest;
import com.ibb.yurtlar.dto.DormitoryResponse;
import com.ibb.yurtlar.dto.UpdateDormitoryRequest;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.exception.DormitoryAlreadyExistsException;
import com.ibb.yurtlar.exception.DormitoryNotFoundException;
import com.ibb.yurtlar.repository.DormitoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DormitoryService {

    private final DormitoryRepository dormitoryRepository;

    public DormitoryService(
            DormitoryRepository dormitoryRepository
    ) {
        this.dormitoryRepository = dormitoryRepository;
    }

    @Transactional
    public DormitoryResponse create(
            CreateDormitoryRequest request
    ) {
        String normalizedName =
                normalizeRequiredText(request.name());

        if (dormitoryRepository
                .existsByNameIgnoreCase(normalizedName)) {

            throw new DormitoryAlreadyExistsException(
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
            UpdateDormitoryRequest request
    ) {
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
            throw new DormitoryAlreadyExistsException(
                    normalizedName
            );
        }

        dormitory.setName(normalizedName);
        dormitory.setAddress(
                normalizeOptionalText(request.address())
        );
        dormitory.setCapacity(request.capacity());
        dormitory.setActive(request.active());

        return toResponse(dormitory);
    }

    private Dormitory findDormitoryById(Long id) {
        return dormitoryRepository
                .findById(id)
                .orElseThrow(
                        () -> new DormitoryNotFoundException(id)
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
}