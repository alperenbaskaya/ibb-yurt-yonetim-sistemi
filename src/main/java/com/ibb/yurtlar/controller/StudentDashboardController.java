package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.StudentDashboardResponse;
import com.ibb.yurtlar.service.StudentDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student-dashboard")
public class StudentDashboardController {

    private final StudentDashboardService
            studentDashboardService;

    public StudentDashboardController(
            StudentDashboardService studentDashboardService
    ) {
        this.studentDashboardService =
                studentDashboardService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentDashboardResponse getMyDashboard(
            Authentication authentication
    ) {
        return studentDashboardService
                .getMyDashboard(
                        authentication.getName()
                );
    }
}