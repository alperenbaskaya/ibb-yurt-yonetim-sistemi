package com.ibb.yurtlar.service;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.*;

import com.ibb.yurtlar.exception.BusinessException;

import com.ibb.yurtlar.dto.CreateStudentRequest;
import com.ibb.yurtlar.dto.StudentResponse;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Student;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.StudentRepository;
import com.ibb.yurtlar.repository.DormitoryTermRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.enums.AdminScope;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final AppUserRepository appUserRepository;
    private final DormitoryTermRepository dormitoryTermRepository;

    public StudentService(
            StudentRepository studentRepository,
            AppUserRepository appUserRepository,
            DormitoryTermRepository dormitoryTermRepository
    ) {
        this.studentRepository = studentRepository;
        this.appUserRepository = appUserRepository;
        this.dormitoryTermRepository = dormitoryTermRepository;
    }

    @Transactional
    public StudentResponse create(
            CreateStudentRequest request,
            String adminEmail
    ) {
        AppUser admin =
                findAuthenticatedAdmin(
                        adminEmail
                );

        if (admin.getAdminScope()
                != AdminScope.GLOBAL) {

            throw new BusinessException(STUDENT_MANAGEMENT_ACCESS_DENIED_MESSAGE, "Yalnızca GLOBAL admin öğrenci profili oluşturabilir."
            );
        }

        AppUser user =
                appUserRepository
                        .findById(
                                request.userId()
                        )
                        .orElseThrow(
                                () -> new BusinessException(USER_NOT_FOUND,
                                        request.userId()
                                )
                        );

        if (user.getRole() != Role.STUDENT) {
            throw new BusinessException(USER_IS_NOT_STUDENT,
                    user.getId()
            );
        }

        if (studentRepository
                .existsByUserId(
                        user.getId()
                )) {

            throw new BusinessException(STUDENT_PROFILE_ALREADY_EXISTS,
                    user.getId()
            );
        }

        String identityNumber =
                request.identityNumber()
                        .trim();

        if (studentRepository
                .existsByIdentityNumber(
                        identityNumber
                )) {

            throw new BusinessException(IDENTITY_NUMBER_ALREADY_EXISTS,
                    identityNumber
            );
        }

        Student student =
                new Student();

        student.setIdentityNumber(
                identityNumber
        );

        student.setFaculty(
                request.faculty().trim()
        );

        student.setDepartment(
                request.department().trim()
        );

        student.setPhone(
                request.phone().trim()
        );

        student.setBirthDate(
                request.birthDate()
        );

        student.setUser(
                user
        );

        Student savedStudent =
                studentRepository.save(
                        student
                );

        return toResponse(
                savedStudent
        );
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAll(
            String adminEmail
    ) {
        AppUser admin =
                findAuthenticatedAdmin(
                        adminEmail
                );

        if (admin.getAdminScope()
                == AdminScope.GLOBAL) {

            return studentRepository
                    .findAll()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        Dormitory adminDormitory =
                getDormitoryAdminDormitory(
                        admin
                );

        Long activeTermId = dormitoryTermRepository
                .findByActiveTrue()
                .orElseThrow(() -> new BusinessException(ACTIVE_DORMITORY_TERM_NOT_FOUND))
                .getId();

        return studentRepository
                .findApprovedStudentsByDormitoryAndTerm(
                        adminDormitory.getId(),
                        activeTermId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getGlobalStudentsByDormitory(
            Long dormitoryId,
            String adminEmail
    ) {
        AppUser admin = findAuthenticatedAdmin(adminEmail);

        if (admin.getAdminScope() != AdminScope.GLOBAL) {
            throw new BusinessException(STUDENT_MANAGEMENT_ACCESS_DENIED_MESSAGE, "Yalnızca GLOBAL admin yurt bazlı öğrenci listesini görüntüleyebilir."
            );
        }

        Long activeTermId = dormitoryTermRepository
                .findByActiveTrue()
                .orElseThrow(() -> new BusinessException(ACTIVE_DORMITORY_TERM_NOT_FOUND))
                .getId();

        return studentRepository
                .findApprovedStudentsByDormitoryAndTerm(
                        dormitoryId,
                        activeTermId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse getById(
            Long id,
            String adminEmail
    ) {
        AppUser admin =
                findAuthenticatedAdmin(
                        adminEmail
                );

        Student student;

        if (admin.getAdminScope()
                == AdminScope.GLOBAL) {

            student =
                    findStudentById(
                            id
                    );
        } else {
            Dormitory adminDormitory =
                    getDormitoryAdminDormitory(
                            admin
                    );

            student =
                    studentRepository
                            .findActiveTermStudentByIdAndDormitory(
                                    id,
                                    adminDormitory.getId()
                            )
                            .orElseThrow(
                                    () -> new BusinessException(STUDENT_MANAGEMENT_ACCESS_DENIED,
                                            id
                                    )
                            );
        }

        return toResponse(
                student
        );
    }

    private Student findStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(
                        () -> new BusinessException(STUDENT_NOT_FOUND, id)
                );
    }

    private StudentResponse toResponse(Student student) {
        AppUser user = student.getUser();

        return new StudentResponse(
                student.getId(),
                student.getIdentityNumber(),
                student.getFaculty(),
                student.getDepartment(),
                student.getPhone(),
                student.getBirthDate(),
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
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


}
