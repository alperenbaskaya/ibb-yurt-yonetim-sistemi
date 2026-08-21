package com.ibb.yurtlar.service;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.*;

import com.ibb.yurtlar.exception.BusinessException;

import com.ibb.yurtlar.dto.DocumentCompletionResponse;
import com.ibb.yurtlar.dto.StudentDashboardResponse;
import com.ibb.yurtlar.dto.StudentDocumentActionRequiredResponse;
import com.ibb.yurtlar.dto.StudentDocumentRequirementStatusResponse;
import com.ibb.yurtlar.dto.StudentLastReviewResponse;
import com.ibb.yurtlar.entity.Admission;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.DocumentReview;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.entity.Student;
import com.ibb.yurtlar.enums.AdmissionStatus;
import com.ibb.yurtlar.enums.StudentDocumentActionReason;
import com.ibb.yurtlar.enums.StudentDocumentStatus;
import com.ibb.yurtlar.enums.UploadPeriodStatus;
import com.ibb.yurtlar.repository.AdmissionRepository;
import com.ibb.yurtlar.repository.DocumentReviewRepository;
import com.ibb.yurtlar.repository.StudentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class StudentDashboardService {

    private final StudentRepository
            studentRepository;

    private final AdmissionRepository
            admissionRepository;

    private final StudentDocumentService
            studentDocumentService;

    private final DocumentReviewRepository
            documentReviewRepository;

    public StudentDashboardService(
            StudentRepository studentRepository,
            AdmissionRepository admissionRepository,
            StudentDocumentService studentDocumentService,
            DocumentReviewRepository documentReviewRepository
    ) {
        this.studentRepository =
                studentRepository;

        this.admissionRepository =
                admissionRepository;

        this.studentDocumentService =
                studentDocumentService;

        this.documentReviewRepository =
                documentReviewRepository;
    }

    @Transactional(readOnly = true)
    public StudentDashboardResponse getMyDashboard(
            String email
    ) {
        Student student =
                studentRepository
                        .findByUserEmail(email)
                        .orElseThrow(
                                () -> new BusinessException(STUDENT_NOT_FOUND,
                                        (Object) null
                                )
                        );

        Admission admission =
                admissionRepository
                        .findByStudent_IdAndDormitoryTerm_ActiveTrue(
                                student.getId()
                        )
                        .orElseThrow(
                                () -> new BusinessException(ACTIVE_ADMISSION_NOT_FOUND,
                                        student.getId()
                                )
                        );

        List<StudentDocumentRequirementStatusResponse>
                documentStatuses =
                studentDocumentService
                        .getRequirementStatusesByAdmissionId(
                                admission.getId()
                        );

        DocumentCompletionResponse completion =
                studentDocumentService
                        .getCompletionStatus(
                                admission.getId()
                        );

        List<StudentDocumentActionRequiredResponse>
                actionRequiredDocuments =
                createActionRequiredDocuments(
                        documentStatuses
                );

        StudentLastReviewResponse lastReview =
                findLastReview(
                        admission.getId()
                );

        AppUser user =
                student.getUser();

        DormitoryTerm term =
                admission.getDormitoryTerm();

        Dormitory dormitory =
                admission.getDormitory();

        LocalDate uploadStartDate =
                term.getDocumentUploadStartDate();

        LocalDate uploadEndDate =
                term.getDocumentUploadEndDate();

        UploadPeriodStatus uploadPeriodStatus =
                determineUploadPeriodStatus(
                        uploadStartDate,
                        uploadEndDate
                );

        long remainingUploadDays =
                calculateRemainingUploadDays(
                        uploadEndDate
                );

        int completionPercentage =
                calculateCompletionPercentage(
                        completion.totalRequiredDocuments(),
                        completion.approvedRequiredDocuments()
                );

        String admissionStatusMessage =
                createAdmissionStatusMessage(
                        admission.getStatus()
                );

        return new StudentDashboardResponse(
                student.getId(),
                user.getId(),

                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),

                admission.getId(),
                admission.getStatus(),
                admissionStatusMessage,

                dormitory.getId(),
                dormitory.getName(),

                term.getId(),
                term.getName(),

                uploadStartDate,
                uploadEndDate,
                remainingUploadDays,
                uploadPeriodStatus,

                completion.totalRequiredDocuments(),
                completion.approvedRequiredDocuments(),
                completionPercentage,
                completion.completed(),

                lastReview,

                actionRequiredDocuments,

                documentStatuses
        );
    }

    private UploadPeriodStatus determineUploadPeriodStatus(
            LocalDate uploadStartDate,
            LocalDate uploadEndDate
    ) {
        LocalDate today =
                LocalDate.now();

        if (today.isBefore(uploadStartDate)) {
            return UploadPeriodStatus.NOT_STARTED;
        }

        if (today.isAfter(uploadEndDate)) {
            return UploadPeriodStatus.CLOSED;
        }

        return UploadPeriodStatus.OPEN;
    }

    private long calculateRemainingUploadDays(
            LocalDate uploadEndDate
    ) {
        LocalDate today =
                LocalDate.now();

        if (today.isAfter(uploadEndDate)) {
            return 0;
        }

        return ChronoUnit.DAYS.between(
                today,
                uploadEndDate
        );
    }

    private int calculateCompletionPercentage(
            int totalRequiredDocuments,
            int approvedRequiredDocuments
    ) {
        if (totalRequiredDocuments == 0) {
            return 0;
        }

        return (int) Math.round(
                approvedRequiredDocuments
                        * 100.0
                        / totalRequiredDocuments
        );
    }

    private String createAdmissionStatusMessage(
            AdmissionStatus admissionStatus
    ) {
        return switch (admissionStatus) {

            case PENDING ->
                    "Yurt kabul başvurunuz değerlendirme aşamasındadır.";

            case APPROVED ->
                    "Yurt kabulünüz onaylanmıştır. "
                            + "Gerekli belgeleri belirtilen tarihler "
                            + "arasında yükleyebilirsiniz.";

            case REJECTED ->
                    "Yurt kabul başvurunuz reddedilmiştir.";
        };
    }

    private StudentLastReviewResponse findLastReview(
            Long admissionId
    ) {
        List<DocumentReview> reviews =
                documentReviewRepository
                        .findRecentByAdmissionId(
                                admissionId,
                                PageRequest.of(0, 1)
                        );

        if (reviews.isEmpty()) {
            return null;
        }

        DocumentReview review =
                reviews.getFirst();

        return new StudentLastReviewResponse(
                review.getId(),

                review.getStudentDocument().getId(),

                review.getStudentDocument()
                        .getDocumentType()
                        .getId(),

                review.getStudentDocument()
                        .getDocumentType()
                        .getName(),

                review.getDecision(),
                review.getComment(),
                review.getReviewedAt()
        );
    }

    private List<StudentDocumentActionRequiredResponse>
    createActionRequiredDocuments(
            List<StudentDocumentRequirementStatusResponse>
                    documentStatuses
    ) {
        return documentStatuses
                .stream()

                .filter(
                        StudentDocumentRequirementStatusResponse
                                ::required
                )

                .filter(
                        this::requiresStudentAction
                )

                .map(
                        this::toActionRequiredResponse
                )

                .toList();
    }

    private boolean requiresStudentAction(
            StudentDocumentRequirementStatusResponse status
    ) {
        if (!status.uploaded()) {
            return true;
        }

        return status.status()
                == StudentDocumentStatus.REVISION_REQUIRED

                || status.status()
                == StudentDocumentStatus.REJECTED;
    }

    private StudentDocumentActionRequiredResponse
    toActionRequiredResponse(
            StudentDocumentRequirementStatusResponse status
    ) {
        if (!status.uploaded()) {
            return new StudentDocumentActionRequiredResponse(
                    status.documentTypeId(),
                    status.documentTypeName(),

                    null,

                    StudentDocumentActionReason.MISSING,

                    "Bu zorunlu belge henüz yüklenmedi."
            );
        }

        if (status.status()
                == StudentDocumentStatus.REVISION_REQUIRED) {

            return new StudentDocumentActionRequiredResponse(
                    status.documentTypeId(),
                    status.documentTypeName(),

                    status.studentDocumentId(),

                    StudentDocumentActionReason.REVISION_REQUIRED,

                    "Bu belge için revizyon istenmiştir. "
                            + "Belgeyi güncelleyip tekrar yüklemelisiniz."
            );
        }

        return new StudentDocumentActionRequiredResponse(
                status.documentTypeId(),
                status.documentTypeName(),

                status.studentDocumentId(),

                StudentDocumentActionReason.REJECTED,

                "Bu belge reddedilmiştir. "
                        + "Geçerli bir belgeyi tekrar yüklemelisiniz."
        );
    }
}
