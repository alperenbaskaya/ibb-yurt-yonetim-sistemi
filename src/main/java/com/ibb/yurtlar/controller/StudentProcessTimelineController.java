package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.StudentProcessTimelineResponse;
import com.ibb.yurtlar.service.StudentProcessTimelineService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student-history")
@PreAuthorize("hasRole('STUDENT')")
public class StudentProcessTimelineController {
    private final StudentProcessTimelineService timelineService;

    public StudentProcessTimelineController(StudentProcessTimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @GetMapping("/me")
    public StudentProcessTimelineResponse getMyTimeline(Authentication authentication) {
        return timelineService.getMyTimeline(authentication.getName());
    }
}
