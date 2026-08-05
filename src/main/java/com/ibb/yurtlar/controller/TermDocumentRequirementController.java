package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.CreateTermDocumentRequirementRequest;
import com.ibb.yurtlar.dto.TermDocumentRequirementResponse;
import com.ibb.yurtlar.dto.UpdateTermDocumentRequirementRequest;
import com.ibb.yurtlar.service.TermDocumentRequirementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/term-document-requirements")
public class TermDocumentRequirementController {

    private final TermDocumentRequirementService
            termDocumentRequirementService;

    public TermDocumentRequirementController(
            TermDocumentRequirementService
                    termDocumentRequirementService
    ) {
        this.termDocumentRequirementService =
                termDocumentRequirementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TermDocumentRequirementResponse create(
            @Valid
            @RequestBody
            CreateTermDocumentRequirementRequest request
    ) {
        return termDocumentRequirementService.create(request);
    }

    @GetMapping("/{id}")
    public TermDocumentRequirementResponse getById(
            @PathVariable Long id
    ) {
        return termDocumentRequirementService.getById(id);
    }

    @GetMapping("/term/{dormitoryTermId}")
    public List<TermDocumentRequirementResponse> getAllByTerm(
            @PathVariable Long dormitoryTermId
    ) {
        return termDocumentRequirementService
                .getAllByTerm(dormitoryTermId);
    }

    @GetMapping("/term/{dormitoryTermId}/active")
    public List<TermDocumentRequirementResponse>
    getActiveRequirementsByTerm(
            @PathVariable Long dormitoryTermId
    ) {
        return termDocumentRequirementService
                .getActiveRequirementsByTerm(
                        dormitoryTermId
                );
    }

    @PatchMapping("/{id}")
    public TermDocumentRequirementResponse update(
            @PathVariable Long id,
            @Valid
            @RequestBody
            UpdateTermDocumentRequirementRequest request
    ) {
        return termDocumentRequirementService
                .update(id, request);
    }
}