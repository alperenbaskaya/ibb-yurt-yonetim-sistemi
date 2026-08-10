package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.ReviewerStudentResponse;
import com.ibb.yurtlar.service.ReviewerStudentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviewer-students")
public class ReviewerStudentController {

    private final ReviewerStudentService reviewerStudentService;

    public ReviewerStudentController(
            ReviewerStudentService reviewerStudentService
    ) {
        this.reviewerStudentService = reviewerStudentService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('REVIEWER')")
    public List<ReviewerStudentResponse> getMyDormitoryStudents(
            Authentication authentication
    ) {
        return reviewerStudentService.getMyDormitoryStudents(
                authentication.getName()
        );
    }
}
