package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.DocumentCompletionResponse;
import com.ibb.yurtlar.dto.DownloadedFile;
import com.ibb.yurtlar.dto.StudentDocumentRequirementStatusResponse;
import com.ibb.yurtlar.dto.StudentDocumentResponse;
import com.ibb.yurtlar.service.StudentDocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/student-documents")
public class StudentDocumentController {

    private final StudentDocumentService
            studentDocumentService;

    public StudentDocumentController(
            StudentDocumentService studentDocumentService
    ) {
        this.studentDocumentService =
                studentDocumentService;
    }

    @PostMapping(
            value = "/me/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('STUDENT')")
    public StudentDocumentResponse uploadMyDocument(
            @RequestParam Long documentTypeId,
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) {
        return studentDocumentService
                .uploadMyDocument(
                        authentication.getName(),
                        documentTypeId,
                        file
                );
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public List<StudentDocumentResponse> getMyDocuments(
            Authentication authentication
    ) {
        return studentDocumentService
                .getMyDocuments(
                        authentication.getName()
                );
    }

    @GetMapping("/me/requirements")
    @PreAuthorize("hasRole('STUDENT')")
    public List<StudentDocumentRequirementStatusResponse>
    getMyRequirementStatuses(
            Authentication authentication
    ) {
        return studentDocumentService
                .getMyRequirementStatuses(
                        authentication.getName()
                );
    }

    @GetMapping("/me/completion")
    @PreAuthorize("hasRole('STUDENT')")
    public DocumentCompletionResponse
    getMyCompletionStatus(
            Authentication authentication
    ) {
        return studentDocumentService
                .getMyCompletionStatus(
                        authentication.getName()
                );
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'REVIEWER', 'STUDENT')"
    )
    public StudentDocumentResponse getById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return studentDocumentService
                .getByIdForAuthenticatedUser(
                        id,
                        authentication.getName()
                );
    }

    @GetMapping("/admission/{admissionId}")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'REVIEWER')"
    )
    public List<StudentDocumentResponse>
    getByAdmissionId(
            @PathVariable Long admissionId,
            Authentication authentication
    ) {
        return studentDocumentService
                .getByAdmissionIdForAuthenticatedUser(
                        admissionId,
                        authentication.getName()
                );
    }

    @GetMapping("/{id}/download")
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'REVIEWER', 'STUDENT')"
    )
    public ResponseEntity<Resource> download(
            @PathVariable Long id,
            Authentication authentication
    ) {
        DownloadedFile downloadedFile =
                studentDocumentService
                        .downloadForAuthenticatedUser(
                                id,
                                authentication.getName()
                        );

        MediaType mediaType;

        try {
            mediaType =
                    MediaType.parseMediaType(
                            downloadedFile.contentType()
                    );
        } catch (Exception exception) {
            mediaType =
                    MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\""
                                + downloadedFile.originalFileName()
                                + "\""
                )
                .body(
                        downloadedFile.resource()
                );
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('REVIEWER')")
    public List<StudentDocumentResponse>
    getPendingDocuments(
            Authentication authentication
    ) {
        return studentDocumentService
                .getPendingDocumentsForReviewer(
                        authentication.getName()
                );
    }

    @GetMapping(
            "/admission/{admissionId}/requirements"
    )
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'REVIEWER')"
    )
    public List<StudentDocumentRequirementStatusResponse>
    getRequirementStatusesByAdmissionId(
            @PathVariable Long admissionId,
            Authentication authentication
    ) {
        return studentDocumentService
                .getRequirementStatusesForAuthenticatedUser(
                        admissionId,
                        authentication.getName()
                );
    }

    @GetMapping(
            "/admission/{admissionId}/completion"
    )
    @PreAuthorize(
            "hasAnyRole('ADMIN', 'REVIEWER')"
    )
    public DocumentCompletionResponse getCompletionStatus(
            @PathVariable Long admissionId,
            Authentication authentication
    ) {
        return studentDocumentService
                .getCompletionStatusForAuthenticatedUser(
                        admissionId,
                        authentication.getName()
                );
    }
}