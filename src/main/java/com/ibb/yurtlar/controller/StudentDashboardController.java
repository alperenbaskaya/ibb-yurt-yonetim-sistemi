package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.StudentDashboardResponse;
import com.ibb.yurtlar.service.StudentDashboardService;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{studentId}")
    public StudentDashboardResponse getDashboard(
            @PathVariable Long studentId
    ) {
        return studentDashboardService
                .getDashboard(studentId);
    }
}