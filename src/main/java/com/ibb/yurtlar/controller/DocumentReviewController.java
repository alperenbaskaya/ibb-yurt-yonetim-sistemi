package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.CreateDocumentReviewRequest;
import com.ibb.yurtlar.dto.DocumentReviewResponse;
import com.ibb.yurtlar.service.DocumentReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/document-reviews")
public class DocumentReviewController {

    private final DocumentReviewService
            documentReviewService;

    public DocumentReviewController(
            DocumentReviewService documentReviewService
    ) {
        this.documentReviewService =
                documentReviewService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('REVIEWER')")
    public DocumentReviewResponse create(
            @Valid
            @RequestBody
            CreateDocumentReviewRequest request,

            Authentication authentication
    ) {
        return documentReviewService.create(
                request,
                authentication.getName()
        );
    }

    @GetMapping("/document/{studentDocumentId}")
    @PreAuthorize("hasRole('REVIEWER')")
    public List<DocumentReviewResponse> getByDocumentId(
            @PathVariable Long studentDocumentId
    ) {
        return documentReviewService
                .getByDocumentId(studentDocumentId);
    }

    @GetMapping("/reviewer/{reviewerUserId}")
    @PreAuthorize("hasRole('REVIEWER')")
    public List<DocumentReviewResponse> getByReviewerId(
            @PathVariable Long reviewerUserId
    ) {
        return documentReviewService
                .getByReviewerId(reviewerUserId);
    }
}