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
import com.ibb.yurtlar.entity.Dormitory;

import java.util.List;

@Service
public class AdmissionService {

    private final AdmissionRepository admissionRepository;
    private final StudentRepository studentRepository;
    private final DormitoryTermRepository dormitoryTermRepository;
    private final DormitoryRepository dormitoryRepository;


    public AdmissionService(
            AdmissionRepository admissionRepository,
            StudentRepository studentRepository,
            DormitoryTermRepository dormitoryTermRepository,
            DormitoryRepository dormitoryRepository
    ) {
        this.admissionRepository = admissionRepository;
        this.studentRepository = studentRepository;
        this.dormitoryTermRepository = dormitoryTermRepository;
        this.dormitoryRepository = dormitoryRepository;
    }

    @Transactional
    public AdmissionResponse create(
            CreateAdmissionRequest request
    ) {
        Student student = studentRepository
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

        if (!dormitory.isActive()) {
            throw new InactiveDormitoryException(
                    dormitory.getId()
            );
        }

        Admission admission = new Admission();

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
                admissionRepository.save(admission);

        return toResponse(savedAdmission);
    }

    @Transactional(readOnly = true)
    public List<AdmissionResponse> getAll() {
        return admissionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdmissionResponse getById(Long id) {
        return toResponse(findAdmissionById(id));
    }

    @Transactional(readOnly = true)
    public List<AdmissionResponse> getByStudentId(
            Long studentId
    ) {
        if (!studentRepository.existsById(studentId)) {
            throw new StudentNotFoundException(studentId);
        }

        return admissionRepository
                .findAllByStudent_IdOrderByDormitoryTerm_StartDateDesc(
                        studentId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AdmissionResponse updateStatus(
            Long id,
            UpdateAdmissionStatusRequest request
    ) {
        Admission admission = findAdmissionById(id);

        admission.setStatus(request.status());

        return toResponse(admission);
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
    public void delete(Long id) {
        Admission admission = findAdmissionById(id);

        admissionRepository.delete(admission);
    }

    @Transactional(readOnly = true)
    public List<AdmissionResponse> filter(
            Long termId,
            AdmissionStatus status,
            String dormitoryName
    ) {
        if (termId != null
                && !dormitoryTermRepository.existsById(termId)) {

            throw new DormitoryTermNotFoundException(termId);
        }

        String normalizedDormitoryName =
                normalizeOptionalText(dormitoryName);

        return admissionRepository
                .findByFilters(
                        termId,
                        status,
                        normalizedDormitoryName
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
    getCurrentTermSummary() {

        DormitoryTerm activeTerm =
                dormitoryTermRepository
                        .findByActiveTrue()
                        .orElseThrow(
                                ActiveDormitoryTermNotFoundException::new
                        );

        Long termId = activeTerm.getId();

        long pendingCount =
                admissionRepository
                        .countByDormitoryTerm_IdAndStatus(
                                termId,
                                AdmissionStatus.PENDING
                        );

        long approvedCount =
                admissionRepository
                        .countByDormitoryTerm_IdAndStatus(
                                termId,
                                AdmissionStatus.APPROVED
                        );

        long rejectedCount =
                admissionRepository
                        .countByDormitoryTerm_IdAndStatus(
                                termId,
                                AdmissionStatus.REJECTED
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
            AdmissionStatus status
    ) {
        DormitoryTerm activeTerm =
                dormitoryTermRepository
                        .findByActiveTrue()
                        .orElseThrow(
                                ActiveDormitoryTermNotFoundException::new
                        );

        String normalizedDormitoryName =
                normalizeOptionalText(dormitoryName);

        return admissionRepository
                .findByFilters(
                        activeTerm.getId(),
                        status,
                        normalizedDormitoryName
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

}