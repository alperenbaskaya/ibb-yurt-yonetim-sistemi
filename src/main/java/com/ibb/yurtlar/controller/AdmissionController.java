package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.AdmissionResponse;
import com.ibb.yurtlar.dto.CreateAdmissionRequest;
import com.ibb.yurtlar.dto.CurrentTermAdmissionSummaryResponse;
import com.ibb.yurtlar.dto.UpdateAdmissionStatusRequest;
import com.ibb.yurtlar.enums.AdmissionStatus;
import com.ibb.yurtlar.service.AdmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admissions")
@PreAuthorize("hasRole('ADMIN')")
public class AdmissionController {

    private final AdmissionService
            admissionService;

    public AdmissionController(
            AdmissionService admissionService
    ) {
        this.admissionService =
                admissionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdmissionResponse create(
            @Valid
            @RequestBody
            CreateAdmissionRequest request,
            Authentication authentication
    ) {
        return admissionService.create(
                request,
                authentication.getName()
        );
    }

    @GetMapping
    public List<AdmissionResponse> getAll(
            Authentication authentication
    ) {
        return admissionService.getAll(
                authentication.getName()
        );
    }

    @GetMapping("/{id}")
    public AdmissionResponse getById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return admissionService.getById(
                id,
                authentication.getName()
        );
    }

    @GetMapping("/student/{studentId}")
    public List<AdmissionResponse> getByStudentId(
            @PathVariable Long studentId,
            Authentication authentication
    ) {
        return admissionService.getByStudentId(
                studentId,
                authentication.getName()
        );
    }

    @PatchMapping("/{id}/status")
    public AdmissionResponse updateStatus(
            @PathVariable Long id,
            @Valid
            @RequestBody
            UpdateAdmissionStatusRequest request,
            Authentication authentication
    ) {
        return admissionService.updateStatus(
                id,
                request,
                authentication.getName()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            Authentication authentication
    ) {
        admissionService.delete(
                id,
                authentication.getName()
        );
    }

    @GetMapping("/filter")
    public List<AdmissionResponse> filter(
            @RequestParam(required = false)
            Long termId,

            @RequestParam(required = false)
            AdmissionStatus status,

            @RequestParam(required = false)
            String dormitoryName,

            Authentication authentication
    ) {
        return admissionService.filter(
                termId,
                status,
                dormitoryName,
                authentication.getName()
        );
    }

    @GetMapping("/current-term")
    public List<AdmissionResponse>
    getCurrentTermAdmissions(
            @RequestParam(required = false)
            AdmissionStatus status,

            @RequestParam(required = false)
            String dormitoryName,

            Authentication authentication
    ) {
        return admissionService
                .getCurrentTermAdmissionsByDormitory(
                        dormitoryName,
                        status,
                        authentication.getName()
                );
    }

    @GetMapping("/current-term/summary")
    public CurrentTermAdmissionSummaryResponse
    getCurrentTermSummary(
            Authentication authentication
    ) {
        return admissionService
                .getCurrentTermSummary(
                        authentication.getName()
                );
    }
}