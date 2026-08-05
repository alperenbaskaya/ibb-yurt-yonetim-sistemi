package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.CreateDocumentTypeRequest;
import com.ibb.yurtlar.dto.DocumentTypeResponse;
import com.ibb.yurtlar.dto.UpdateDocumentTypeRequest;
import com.ibb.yurtlar.service.DocumentTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/document-types")
public class DocumentTypeController {

    private final DocumentTypeService documentTypeService;

    public DocumentTypeController(
            DocumentTypeService documentTypeService
    ) {
        this.documentTypeService = documentTypeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentTypeResponse create(
            @Valid
            @RequestBody
            CreateDocumentTypeRequest request
    ) {
        return documentTypeService.create(request);
    }

    @GetMapping
    public List<DocumentTypeResponse> getAll() {
        return documentTypeService.getAll();
    }

    @GetMapping("/active")
    public List<DocumentTypeResponse> getAllActive() {
        return documentTypeService.getAllActive();
    }

    @GetMapping("/{id}")
    public DocumentTypeResponse getById(
            @PathVariable Long id
    ) {
        return documentTypeService.getById(id);
    }

    @PutMapping("/{id}")
    public DocumentTypeResponse update(
            @PathVariable Long id,
            @Valid
            @RequestBody
            UpdateDocumentTypeRequest request
    ) {
        return documentTypeService.update(id, request);
    }
}