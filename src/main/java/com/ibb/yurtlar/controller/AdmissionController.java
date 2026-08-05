package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.AdmissionResponse;
import com.ibb.yurtlar.dto.CreateAdmissionRequest;
import com.ibb.yurtlar.dto.UpdateAdmissionStatusRequest;
import com.ibb.yurtlar.service.AdmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.ibb.yurtlar.enums.AdmissionStatus;
import com.ibb.yurtlar.dto.CurrentTermAdmissionSummaryResponse;

import java.util.List;

@RestController
@RequestMapping("/api/admissions")
public class AdmissionController {

    private final AdmissionService admissionService;

    public AdmissionController(
            AdmissionService admissionService
    ) {
        this.admissionService = admissionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdmissionResponse create(
            @Valid @RequestBody CreateAdmissionRequest request
    ) {
        return admissionService.create(request);
    }

    @GetMapping
    public List<AdmissionResponse> getAll() {
        return admissionService.getAll();
    }

    @GetMapping("/{id}")
    public AdmissionResponse getById(
            @PathVariable Long id
    ) {
        return admissionService.getById(id);
    }

    @GetMapping("/student/{studentId}")
    public List<AdmissionResponse> getByStudentId(
            @PathVariable Long studentId
    ) {
        return admissionService.getByStudentId(studentId);
    }

    @PatchMapping("/{id}/status")
    public AdmissionResponse updateStatus(
            @PathVariable Long id,
            @Valid
            @RequestBody
            UpdateAdmissionStatusRequest request
    ) {
        return admissionService.updateStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {
        admissionService.delete(id);
    }

    @GetMapping("/filter")
    public List<AdmissionResponse> filter(
            @RequestParam(required = false) Long termId,
            @RequestParam(required = false) AdmissionStatus status,
            @RequestParam(required = false) String dormitoryName
    ) {
        return admissionService.filter(
                termId,
                status,
                dormitoryName
        );
    }

    @GetMapping("/current-term")
    public List<AdmissionResponse> getCurrentTermAdmissions(

            @RequestParam(required = false)
            AdmissionStatus status,

            @RequestParam(required = false)
            String dormitoryName
    ) {
        return admissionService
                .getCurrentTermAdmissionsByDormitory(
                        dormitoryName,
                        status
                );
    }

    @GetMapping("/current-term/summary")
    public CurrentTermAdmissionSummaryResponse
    getCurrentTermSummary() {

        return admissionService
                .getCurrentTermSummary();
    }
}