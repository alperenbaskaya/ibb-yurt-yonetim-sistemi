package com.ibb.yurtlar.service;

import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Admission;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.entity.StudentDocument;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.exception.StudentDocumentAccessDeniedException;
import com.ibb.yurtlar.repository.AdmissionRepository;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.DormitoryTermRepository;
import com.ibb.yurtlar.repository.StudentDocumentRepository;
import com.ibb.yurtlar.repository.TermDocumentRequirementRepository;
import com.ibb.yurtlar.mapper.StudentDocumentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class StudentDocumentServiceReviewerAccessTest {

    @Mock StudentDocumentRepository studentDocumentRepository;
    @Mock AdmissionRepository admissionRepository;
    @Mock TermDocumentRequirementRepository termDocumentRequirementRepository;
    @Mock FileStorageService fileStorageService;
    @Mock TransactionalFileLifecycleService transactionalFileLifecycleService;
    @Mock StudentDocumentMapper studentDocumentMapper;
    @Mock AppUserRepository appUserRepository;
    @Mock DormitoryTermRepository dormitoryTermRepository;
    @Mock NotificationService notificationService;
    @Mock AuditLogService auditLogService;

    @InjectMocks StudentDocumentService service;

    private AppUser reviewer;
    private StudentDocument document;

    @BeforeEach
    void setUp() {
        Dormitory reviewerDormitory = new Dormitory();
        reviewerDormitory.setId(10L);

        reviewer = new AppUser();
        reviewer.setRole(Role.REVIEWER);
        reviewer.setActive(true);
        reviewer.setDormitory(reviewerDormitory);

        document = new StudentDocument();
        document.setId(20L);

        DormitoryTerm activeTerm = new DormitoryTerm();
        activeTerm.setId(30L);
        lenient().when(dormitoryTermRepository.findByActiveTrue())
                .thenReturn(Optional.of(activeTerm));
    }

    @Test
    void allowsCurrentApprovedDocumentInReviewerDormitory() {
        when(studentDocumentRepository.isWithinReviewerDocumentScope(20L, 10L, 30L))
                .thenReturn(true);

        assertDoesNotThrow(() ->
                service.validateReviewerDocumentAccess(reviewer, document));
    }

    @Test
    void deniesPreviousTermDocumentInReviewerDormitory() {
        assertReviewerScopeDenied();
    }

    @Test
    void deniesCurrentTermPendingAdmissionDocument() {
        assertReviewerScopeDenied();
    }

    @Test
    void deniesCurrentTermRejectedAdmissionDocument() {
        assertReviewerScopeDenied();
    }

    @Test
    void deniesCurrentApprovedDocumentInAnotherDormitory() {
        assertReviewerScopeDenied();
    }

    @Test
    void preservesGlobalAdminDocumentAccess() {
        AppUser globalAdmin = new AppUser();
        globalAdmin.setRole(Role.ADMIN);
        globalAdmin.setAdminScope(AdminScope.GLOBAL);
        when(studentDocumentRepository.findById(20L)).thenReturn(Optional.of(document));
        when(appUserRepository.findByNormalizedEmail("global@example.com"))
                .thenReturn(Optional.of(globalAdmin));

        assertDoesNotThrow(() -> service.getByIdForAuthenticatedUser(
                20L,
                "global@example.com"
        ));
        verify(studentDocumentRepository, never())
                .isWithinReviewerDocumentScope(anyLong(), anyLong(), anyLong());
    }

    @Test
    void preservesOwnDormitoryAdminDocumentAccess() {
        Dormitory dormitory = new Dormitory();
        dormitory.setId(10L);
        Admission admission = new Admission();
        admission.setDormitory(dormitory);
        document.setAdmission(admission);

        AppUser dormitoryAdmin = new AppUser();
        dormitoryAdmin.setRole(Role.ADMIN);
        dormitoryAdmin.setAdminScope(AdminScope.DORMITORY);
        dormitoryAdmin.setDormitory(dormitory);
        when(studentDocumentRepository.findById(20L)).thenReturn(Optional.of(document));
        when(appUserRepository.findByNormalizedEmail("admin@example.com"))
                .thenReturn(Optional.of(dormitoryAdmin));

        assertDoesNotThrow(() -> service.getByIdForAuthenticatedUser(
                20L,
                "admin@example.com"
        ));
        verify(studentDocumentRepository, never())
                .isWithinReviewerDocumentScope(anyLong(), anyLong(), anyLong());
    }

    private void assertReviewerScopeDenied() {
        when(studentDocumentRepository.isWithinReviewerDocumentScope(20L, 10L, 30L))
                .thenReturn(false);

        assertThrows(
                StudentDocumentAccessDeniedException.class,
                () -> service.validateReviewerDocumentAccess(reviewer, document)
        );
    }
}
