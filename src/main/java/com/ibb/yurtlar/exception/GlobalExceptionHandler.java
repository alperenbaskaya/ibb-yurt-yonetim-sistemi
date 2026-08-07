package com.ibb.yurtlar.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import com.ibb.yurtlar.exception.ActiveAdmissionNotFoundException;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.FORBIDDEN,
                "Bu işlem için yetkiniz bulunmamaktadır.",
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(apiError);
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiError> handleNotificationNotFound(
            NotificationNotFoundException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(apiError);
    }

    @ExceptionHandler(InvalidDocumentReplacementStateException.class)
    public ResponseEntity<ApiError>
    handleInvalidDocumentReplacementState(
            InvalidDocumentReplacementStateException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(apiError);
    }

    @ExceptionHandler(InvalidAdmissionStatusTransitionException.class)
    public ResponseEntity<ApiError>
    handleInvalidAdmissionStatusTransition(
            InvalidAdmissionStatusTransitionException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(apiError);
    }

    @ExceptionHandler(DormitoryTermAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleDormitoryTermAlreadyExists(
            DormitoryTermAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(apiError);
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ApiError> handleInvalidDateRange(
            InvalidDateRangeException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> validationErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(fieldError ->
                        validationErrors.put(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        )
                );

        ApiError apiError = createApiError(
                HttpStatus.BAD_REQUEST,
                "Gönderilen bilgiler doğrulanamadı.",
                request.getRequestURI(),
                validationErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request
    ) {
        //exception.printStackTrace();
        ApiError apiError = createApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Beklenmeyen bir hata oluştu.",
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(apiError);
    }

    private ApiError createApiError(
            HttpStatus status,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {
        return new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                validationErrors
        );
    }

    @ExceptionHandler(DormitoryTermNotFoundException.class)     //  404
    public ResponseEntity<ApiError> handleDormitoryTermNotFound(
            DormitoryTermNotFoundException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(apiError);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class) // e-posta çakışması
    public ResponseEntity<ApiError> handleEmailAlreadyExists(
            EmailAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(apiError);
    }

    @ExceptionHandler(UserNotFoundException.class) // kullanıcı bulunamadı
    public ResponseEntity<ApiError> handleUserNotFound(
            UserNotFoundException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(apiError);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.BAD_REQUEST,
                "İstek gövdesi okunamadı. JSON yapısını ve gönderilen alan değerlerini kontrol edin.",
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiError);
    }

    @ExceptionHandler(
            IdentityNumberAlreadyExistsException.class
    )
    public ResponseEntity<ApiError> handleIdentityNumberAlreadyExists(
            IdentityNumberAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(apiError);
    }

    @ExceptionHandler(
            StudentProfileAlreadyExistsException.class
    )
    public ResponseEntity<ApiError> handleStudentProfileAlreadyExists(
            StudentProfileAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(apiError);
    }

    @ExceptionHandler(UserIsNotStudentException.class)
    public ResponseEntity<ApiError> handleUserIsNotStudent(
            UserIsNotStudentException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiError);
    }

    @ExceptionHandler(StudentNotFoundException.class)
    public ResponseEntity<ApiError> handleStudentNotFound(
            StudentNotFoundException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(apiError);
    }

    @ExceptionHandler(AdmissionAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleAdmissionAlreadyExists(
            AdmissionAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(apiError);
    }

    @ExceptionHandler(AdmissionNotFoundException.class)
    public ResponseEntity<ApiError> handleAdmissionNotFound(
            AdmissionNotFoundException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(apiError);
    }

    @ExceptionHandler(
            DocumentTypeAlreadyExistsException.class
    )
    public ResponseEntity<ApiError> handleDocumentTypeAlreadyExists(
            DocumentTypeAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(apiError);
    }

    @ExceptionHandler(
            DocumentTypeNotFoundException.class
    )
    public ResponseEntity<ApiError> handleDocumentTypeNotFound(
            DocumentTypeNotFoundException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(apiError);
    }

    @ExceptionHandler(
            TermDocumentRequirementAlreadyExistsException.class
    )
    public ResponseEntity<ApiError>
    handleTermDocumentRequirementAlreadyExists(
            TermDocumentRequirementAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(apiError);
    }

    @ExceptionHandler(
            InactiveDocumentTypeException.class
    )
    public ResponseEntity<ApiError> handleInactiveDocumentType(
            InactiveDocumentTypeException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(apiError);
    }


    //ADMISSION ONAYLI DEĞİLSE
    @ExceptionHandler(AdmissionNotApprovedException.class)
    public ResponseEntity<ApiError> handleAdmissionNotApproved(
            AdmissionNotApprovedException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(apiError);
    }


    //DÖNEM AKTİF DEĞİLSE
    @ExceptionHandler(InactiveDormitoryTermException.class)
    public ResponseEntity<ApiError> handleInactiveDormitoryTerm(
            InactiveDormitoryTermException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(apiError);
    }

    @ExceptionHandler(UserIsNotReviewerException.class)
    public ResponseEntity<ApiError> handleUserIsNotReviewer(
            UserIsNotReviewerException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiError);
    }

    @ExceptionHandler(DocumentNotReadyForReviewException.class)
    public ResponseEntity<ApiError> handleDocumentNotReadyForReview(
            DocumentNotReadyForReviewException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(apiError);
    }

    @ExceptionHandler(ReviewCommentRequiredException.class)
    public ResponseEntity<ApiError> handleReviewCommentRequired(
            ReviewCommentRequiredException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiError);
    }


    //YÜKLEME DÖNEMİ KAPALIYSA
    @ExceptionHandler(DocumentUploadClosedException.class)
    public ResponseEntity<ApiError> handleDocumentUploadClosed(
            DocumentUploadClosedException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(apiError);
    }


    //BELGE O DÖENM İÇİN TANINMLI DEĞİLSE
    @ExceptionHandler(DocumentNotRequiredForTermException.class)
    public ResponseEntity<ApiError>
    handleDocumentNotRequiredForTerm(
            DocumentNotRequiredForTermException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiError);
    }


    //DOSYA BOYUTU GEÇERSİZSE
    @ExceptionHandler(InvalidFileSizeException.class)
    public ResponseEntity<ApiError> handleInvalidFileSize(
            InvalidFileSizeException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiError);
    }


    //BOŞ DOSYA VE GEÇERSİZ TÜR
    @ExceptionHandler({
            EmptyFileException.class,
            InvalidFileTypeException.class
    })
    public ResponseEntity<ApiError> handleInvalidFile(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiError);
    }


    //STUDENT DOCUMENT BULUNAMAZSA
    @ExceptionHandler(StudentDocumentNotFoundException.class)
    public ResponseEntity<ApiError> handleStudentDocumentNotFound(
            StudentDocumentNotFoundException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(apiError);
    }

    @ExceptionHandler(DormitoryTermInUseException.class)
    public ResponseEntity<ApiError> handleDormitoryTermInUse(
            DormitoryTermInUseException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(apiError);
    }

    @ExceptionHandler(
            ActiveDormitoryTermNotFoundException.class
    )
    public ResponseEntity<ApiError>
    handleActiveDormitoryTermNotFound(
            ActiveDormitoryTermNotFoundException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(apiError);
    }

    @ExceptionHandler(ActiveAdmissionNotFoundException.class)
    public ResponseEntity<ApiError>
    handleActiveAdmissionNotFound(
            ActiveAdmissionNotFoundException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(apiError);
    }

    @ExceptionHandler(DormitoryAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleDormitoryAlreadyExists(
            DormitoryAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(apiError);
    }

    @ExceptionHandler(DormitoryNotFoundException.class)
    public ResponseEntity<ApiError> handleDormitoryNotFound(
            DormitoryNotFoundException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(apiError);
    }

    @ExceptionHandler(
            InvalidAdminConfigurationException.class
    )
    public ResponseEntity<ApiError>
    handleInvalidAdminConfiguration(
            InvalidAdminConfigurationException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiError);
    }

    @ExceptionHandler(InactiveDormitoryException.class)
    public ResponseEntity<ApiError>
    handleInactiveDormitory(
            InactiveDormitoryException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(apiError);
    }

    @ExceptionHandler(UserIsNotAdminException.class)
    public ResponseEntity<ApiError> handleUserIsNotAdmin(
            UserIsNotAdminException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiError);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(
            InvalidCredentialsException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(apiError);
    }


    @ExceptionHandler(
            InvalidUserConfigurationException.class
    )
    public ResponseEntity<ApiError>
    handleInvalidUserConfiguration(
            InvalidUserConfigurationException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(apiError);
    }

    @ExceptionHandler(
            ReviewerDormitoryAccessDeniedException.class
    )
    public ResponseEntity<ApiError>
    handleReviewerDormitoryAccessDenied(
            ReviewerDormitoryAccessDeniedException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(apiError);
    }

    @ExceptionHandler(
            ActiveAdmissionNotFoundForCurrentStudentException.class
    )
    public ResponseEntity<ApiError>
    handleActiveAdmissionNotFoundForCurrentStudent(
            ActiveAdmissionNotFoundForCurrentStudentException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(apiError);
    }

    @ExceptionHandler(
            StudentDocumentAccessDeniedException.class
    )
    public ResponseEntity<ApiError>
    handleStudentDocumentAccessDenied(
            StudentDocumentAccessDeniedException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(apiError);
    }

    @ExceptionHandler(
            AdmissionAccessDeniedException.class
    )
    public ResponseEntity<ApiError>
    handleAdmissionAccessDenied(
            AdmissionAccessDeniedException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(apiError);
    }

    @ExceptionHandler(
            UserManagementAccessDeniedException.class
    )
    public ResponseEntity<ApiError>
    handleUserManagementAccessDenied(
            UserManagementAccessDeniedException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(apiError);
    }

    @ExceptionHandler(
            StudentManagementAccessDeniedException.class
    )
    public ResponseEntity<ApiError>
    handleStudentManagementAccessDenied(
            StudentManagementAccessDeniedException exception,
            HttpServletRequest request
    ) {
        ApiError apiError = createApiError(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(apiError);
    }

}
