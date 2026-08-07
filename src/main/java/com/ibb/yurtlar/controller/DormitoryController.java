package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.CreateDormitoryRequest;
import com.ibb.yurtlar.dto.DormitoryResponse;
import com.ibb.yurtlar.dto.UpdateDormitoryRequest;
import com.ibb.yurtlar.service.DormitoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dormitories")
@PreAuthorize("hasRole('ADMIN')")
public class DormitoryController {

    private final DormitoryService dormitoryService;

    public DormitoryController(
            DormitoryService dormitoryService
    ) {
        this.dormitoryService = dormitoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DormitoryResponse create(
            @Valid
            @RequestBody
            CreateDormitoryRequest request,
            Authentication authentication
    ) {
        return dormitoryService.create(
                request,
                authentication.getName()
        );
    }

    @GetMapping
    public List<DormitoryResponse> getAll() {
        return dormitoryService.getAll();
    }

    @GetMapping("/active")
    public List<DormitoryResponse> getAllActive() {
        return dormitoryService.getAllActive();
    }

    @GetMapping("/{id}")
    public DormitoryResponse getById(
            @PathVariable Long id
    ) {
        return dormitoryService.getById(id);
    }

    @PutMapping("/{id}")
    public DormitoryResponse update(
            @PathVariable Long id,
            @Valid
            @RequestBody UpdateDormitoryRequest request,
            Authentication authentication
    ) {
        return dormitoryService.update(
                id,
                request,
                authentication.getName()
        );
    }
}
