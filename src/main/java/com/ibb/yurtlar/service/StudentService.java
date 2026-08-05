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
            CreateStudentRequest request
    ) {
        AppUser user = appUserRepository
                .findById(request.userId())
                .orElseThrow(
                        () -> new UserNotFoundException(
                                request.userId()
                        )
                );

        if (user.getRole() != Role.STUDENT) {
            throw new UserIsNotStudentException(user.getId());
        }

        if (studentRepository.existsByUserId(user.getId())) {
            throw new StudentProfileAlreadyExistsException(
                    user.getId()
            );
        }

        String identityNumber =
                request.identityNumber().trim();

        if (studentRepository
                .existsByIdentityNumber(identityNumber)) {

            throw new IdentityNumberAlreadyExistsException(
                    identityNumber
            );
        }

        Student student = new Student();
        student.setIdentityNumber(identityNumber);
        student.setFaculty(request.faculty().trim());
        student.setDepartment(request.department().trim());
        student.setPhone(request.phone().trim());
        student.setBirthDate(request.birthDate());
        student.setUser(user);

        Student savedStudent =
                studentRepository.save(student);

        return toResponse(savedStudent);
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> getAll() {
        return studentRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        Student student = findStudentById(id);

        return toResponse(student);
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
}