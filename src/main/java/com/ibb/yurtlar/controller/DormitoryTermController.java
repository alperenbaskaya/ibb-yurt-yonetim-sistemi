package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.CreateDormitoryTermRequest;
import com.ibb.yurtlar.dto.DormitoryTermResponse;
import com.ibb.yurtlar.dto.UpdateDormitoryTermRequest;
import com.ibb.yurtlar.service.DormitoryTermService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.ibb.yurtlar.dto.UpdateDormitoryTermActiveRequest;

import java.util.List;

@RestController
@RequestMapping("/api/dormitory-terms")
public class DormitoryTermController {

    private final DormitoryTermService dormitoryTermService;

    public DormitoryTermController(
            DormitoryTermService dormitoryTermService
    ) {
        this.dormitoryTermService = dormitoryTermService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DormitoryTermResponse create(
            @Valid @RequestBody CreateDormitoryTermRequest request
    ) {
        return dormitoryTermService.create(request);
    }

    @GetMapping
    public List<DormitoryTermResponse> getAll() {
        return dormitoryTermService.getAll();
    }

    @GetMapping("/{id}")
    public DormitoryTermResponse getById(
            @PathVariable Long id
    ) {
        return dormitoryTermService.getById(id);
    }

    @PutMapping("/{id}")
    public DormitoryTermResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDormitoryTermRequest request
    ) {
        return dormitoryTermService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id
    ) {
        dormitoryTermService.delete(id);
    }

    @PatchMapping("/{id}/active")
    public DormitoryTermResponse updateActiveStatus(
            @PathVariable Long id,
            @Valid
            @RequestBody
            UpdateDormitoryTermActiveRequest request
    ) {
        return dormitoryTermService.updateActiveStatus(
                id,
                request.active()
        );
    }


}