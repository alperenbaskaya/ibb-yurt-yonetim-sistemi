package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.CreateStudentRequest;
import com.ibb.yurtlar.dto.StudentResponse;
import com.ibb.yurtlar.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(
            StudentService studentService
    ) {
        this.studentService = studentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse create(
            @Valid @RequestBody CreateStudentRequest request
    ) {
        return studentService.create(request);
    }

    @GetMapping
    public List<StudentResponse> getAll() {
        return studentService.getAll();
    }

    @GetMapping("/{id}")
    public StudentResponse getById(
            @PathVariable Long id
    ) {
        return studentService.getById(id);
    }
}