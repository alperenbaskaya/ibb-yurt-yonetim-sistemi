package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.CreateStudentRequest;
import com.ibb.yurtlar.dto.StudentResponse;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Student;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.exception.IdentityNumberAlreadyExistsException;
import com.ibb.yurtlar.exception.StudentNotFoundException;
import com.ibb.yurtlar.exception.StudentProfileAlreadyExistsException;
import com.ibb.yurtlar.exception.UserIsNotStudentException;
import com.ibb.yurtlar.exception.UserNotFoundException;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.exception.InvalidAdminConfigurationException;
import com.ibb.yurtlar.exception.InvalidCredentialsException;
import com.ibb.yurtlar.exception.StudentManagementAccessDeniedException;
import com.ibb.yurtlar.exception.UserIsNotAdminException;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final AppUserRepository appUserRepository;

    public StudentService(
            StudentRepository studentRepository,
            AppUserRepository appUserRepository
    ) {
        this.studentRepository = studentRepository;
        this.appUserRepository = appUserRepository;
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

            throw new StudentManagementAccessDeniedException(
                    "Yalnızca GLOBAL admin öğrenci profili oluşturabilir."
            );
        }

        AppUser user =
                appUserRepository
                        .findById(
                                request.userId()
                        )
                        .orElseThrow(
                                () -> new UserNotFoundException(
                                        request.userId()
                                )
                        );

        if (user.getRole() != Role.STUDENT) {
            throw new UserIsNotStudentException(
                    user.getId()
            );
        }

        if (studentRepository
                .existsByUserId(
                        user.getId()
                )) {

            throw new StudentProfileAlreadyExistsException(
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

            throw new IdentityNumberAlreadyExistsException(
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

        return studentRepository
                .findActiveTermStudentsByDormitory(
                        adminDormitory.getId()
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
                                    () -> new StudentManagementAccessDeniedException(
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
                        () -> new StudentNotFoundException(id)
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

    private Dormitory getDormitoryAdminDormitory(
            AppUser admin
    ) {
        if (admin.getAdminScope()
                != AdminScope.DORMITORY) {

            throw new InvalidAdminConfigurationException(
                    "Kullanıcı yurt admini değildir."
            );
        }

        Dormitory dormitory =
                admin.getDormitory();

        if (dormitory == null) {
            throw new InvalidAdminConfigurationException(
                    "Yurt admini için yurt ataması zorunludur."
            );
        }

        return dormitory;
    }


}