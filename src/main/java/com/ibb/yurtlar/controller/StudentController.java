package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.CreateStudentRequest;
import com.ibb.yurtlar.dto.StudentResponse;
import com.ibb.yurtlar.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@PreAuthorize("hasRole('ADMIN')")
public class StudentController {

    private final StudentService
            studentService;

    public StudentController(
            StudentService studentService
    ) {
        this.studentService =
                studentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse create(
            @Valid
            @RequestBody
            CreateStudentRequest request,
            Authentication authentication
    ) {
        return studentService.create(
                request,
                authentication.getName()
        );
    }

    @GetMapping
    public List<StudentResponse> getAll(
            Authentication authentication
    ) {
        return studentService.getAll(
                authentication.getName()
        );
    }

    @GetMapping("/{id}")
    public StudentResponse getById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return studentService.getById(
                id,
                authentication.getName()
        );
    }
}