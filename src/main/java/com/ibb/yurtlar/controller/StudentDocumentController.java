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

    @GetMapping("/{id}")
    public StudentDocumentResponse getById(
            @PathVariable Long id
    ) {
        return studentDocumentService
                .getById(id);
    }

    @GetMapping("/admission/{admissionId}")
    public List<StudentDocumentResponse>
    getByAdmissionId(
            @PathVariable Long admissionId
    ) {
        return studentDocumentService
                .getByAdmissionId(
                        admissionId
                );
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long id
    ) {
        DownloadedFile downloadedFile =
                studentDocumentService
                        .download(id);

        MediaType mediaType;

        try {
            mediaType = MediaType.parseMediaType(
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
    public List<StudentDocumentResponse>
    getPendingDocuments() {
        return studentDocumentService
                .getPendingDocuments();
    }

    @GetMapping(
            "/admission/{admissionId}/requirements"
    )
    public List<StudentDocumentRequirementStatusResponse>
    getRequirementStatusesByAdmissionId(
            @PathVariable Long admissionId
    ) {
        return studentDocumentService
                .getRequirementStatusesByAdmissionId(
                        admissionId
                );
    }

    @GetMapping(
            "/admission/{admissionId}/completion"
    )
    public DocumentCompletionResponse getCompletionStatus(
            @PathVariable Long admissionId
    ) {
        return studentDocumentService
                .getCompletionStatus(
                        admissionId
                );
    }
}