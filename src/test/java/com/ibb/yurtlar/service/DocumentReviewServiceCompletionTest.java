package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.DocumentCompletionResponse;
import com.ibb.yurtlar.entity.Admission;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.entity.Student;
import com.ibb.yurtlar.entity.StudentDocument;
import com.ibb.yurtlar.enums.NotificationReferenceType;
import com.ibb.yurtlar.enums.NotificationType;
import com.ibb.yurtlar.repository.AdmissionRepository;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.DocumentReviewRepository;
import com.ibb.yurtlar.repository.StudentDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentReviewServiceCompletionTest {

    @Mock DocumentReviewRepository documentReviewRepository;
    @Mock StudentDocumentRepository studentDocumentRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock NotificationService notificationService;
    @Mock StudentDocumentService studentDocumentService;
    @Mock AdmissionRepository admissionRepository;
    @Mock AuditLogService auditLogService;

    @InjectMocks DocumentReviewService service;

    @Test
    void locksAdmissionBeforeEvaluatingAndUsesIdempotentCompletionWriters() {
        Admission admission = completedAdmission(100L, 10L, 20L);
        StudentDocument document = new StudentDocument();
        document.setAdmission(admission);

        when(admissionRepository.findByIdForDocumentCompletionUpdate(100L))
                .thenReturn(Optional.of(admission));
        when(studentDocumentService.getCompletionStatus(100L))
                .thenReturn(new DocumentCompletionResponse(100L, 2, 2, true));
        AppUser dormitoryAdmin = new AppUser();
        dormitoryAdmin.setId(30L);
        when(appUserRepository.findActiveDormitoryAdminsByDormitory(20L))
                .thenReturn(List.of(dormitoryAdmin));

        service.checkDocumentProcessCompletion(document, "reviewer@example.com");

        InOrder order = inOrder(admissionRepository, studentDocumentService);
        order.verify(admissionRepository)
                .findByIdForDocumentCompletionUpdate(100L);
        order.verify(studentDocumentService).getCompletionStatus(100L);
        verify(notificationService).createNotificationIfAbsent(
                10L,
                NotificationType.DOCUMENT_PROCESS_COMPLETED,
                "Belge Süreciniz Tamamlandı",
                "Tüm zorunlu belgeleriniz onaylandı. Yurt kayıt süreciniz tamamlandı ve yurda giriş yapabilirsiniz.",
                NotificationReferenceType.ADMISSION,
                100L
        );
        verify(notificationService).createNotificationIfAbsent(
                30L,
                NotificationType.DOCUMENT_PROCESS_COMPLETED,
                "Öğrenci Belge Sürecini Tamamladı",
                "Ada Yılmaz adlı öğrencinin tüm zorunlu belgeleri onaylandı. Öğrenci yurda giriş için hazır.",
                NotificationReferenceType.ADMISSION,
                100L
        );
        verify(auditLogService).recordDocumentProcessCompletedIfAbsent(
                "reviewer@example.com",
                admission,
                "Ada Yılmaz öğrencisinin zorunlu belge süreci tamamlandı."
        );
    }

    @Test
    void repeatedEvaluationUsesTheSameAdmissionScopedIdentities() {
        Admission admission = completedAdmission(101L, 11L, 21L);
        StudentDocument document = new StudentDocument();
        document.setAdmission(admission);

        when(admissionRepository.findByIdForDocumentCompletionUpdate(101L))
                .thenReturn(Optional.of(admission));
        when(studentDocumentService.getCompletionStatus(101L))
                .thenReturn(new DocumentCompletionResponse(101L, 1, 1, true));
        when(appUserRepository.findActiveDormitoryAdminsByDormitory(21L))
                .thenReturn(List.of());

        service.checkDocumentProcessCompletion(document, "reviewer@example.com");
        service.checkDocumentProcessCompletion(document, "reviewer@example.com");

        verify(admissionRepository, times(2))
                .findByIdForDocumentCompletionUpdate(101L);
        verify(notificationService, times(2)).createNotificationIfAbsent(
                11L,
                NotificationType.DOCUMENT_PROCESS_COMPLETED,
                "Belge Süreciniz Tamamlandı",
                "Tüm zorunlu belgeleriniz onaylandı. Yurt kayıt süreciniz tamamlandı ve yurda giriş yapabilirsiniz.",
                NotificationReferenceType.ADMISSION,
                101L
        );
        verify(auditLogService, times(2)).recordDocumentProcessCompletedIfAbsent(
                anyString(),
                org.mockito.ArgumentMatchers.same(admission),
                anyString()
        );
    }

    private Admission completedAdmission(
            Long admissionId,
            Long studentUserId,
            Long dormitoryId
    ) {
        AppUser studentUser = new AppUser();
        studentUser.setId(studentUserId);
        studentUser.setFirstName("Ada");
        studentUser.setLastName("Yılmaz");

        Student student = new Student();
        student.setUser(studentUser);

        Dormitory dormitory = new Dormitory();
        dormitory.setId(dormitoryId);

        Admission admission = new Admission();
        admission.setId(admissionId);
        admission.setStudent(student);
        admission.setDormitory(dormitory);
        return admission;
    }
}
