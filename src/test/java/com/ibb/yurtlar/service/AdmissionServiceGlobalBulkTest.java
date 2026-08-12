package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.AdmissionPageResponse;
import com.ibb.yurtlar.dto.BulkApproveAdmissionsRequest;
import com.ibb.yurtlar.entity.Admission;
import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.entity.Student;
import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.AdmissionStatus;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.exception.AdmissionAccessDeniedException;
import com.ibb.yurtlar.exception.AdmissionNotFoundException;
import com.ibb.yurtlar.exception.InvalidAdmissionStatusTransitionException;
import com.ibb.yurtlar.exception.UserIsNotAdminException;
import com.ibb.yurtlar.repository.AdmissionRepository;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.DormitoryRepository;
import com.ibb.yurtlar.repository.DormitoryTermRepository;
import com.ibb.yurtlar.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdmissionServiceGlobalBulkTest {

    @Mock AdmissionRepository admissionRepository;
    @Mock StudentRepository studentRepository;
    @Mock DormitoryTermRepository dormitoryTermRepository;
    @Mock DormitoryRepository dormitoryRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock NotificationService notificationService;
    @Mock AuditLogService auditLogService;

    private AdmissionService service;
    private DormitoryTerm activeTerm;

    @BeforeEach
    void setUp() {
        service = new AdmissionService(
                admissionRepository, studentRepository, dormitoryTermRepository,
                dormitoryRepository, appUserRepository, notificationService, auditLogService
        );
        activeTerm = term(100L, true);
    }

    @Test
    void globalPaginationUsesActiveTermStatusSizeTenAndReturnsMetadata() {
        authenticateGlobalAdmin();
        when(dormitoryTermRepository.findByActiveTrue()).thenReturn(Optional.of(activeTerm));
        List<Admission> content = java.util.stream.LongStream.rangeClosed(1, 10)
                .mapToObj(id -> admission(id, activeTerm, AdmissionStatus.PENDING)).toList();
        when(admissionRepository.findGlobalCurrentTermPage(eq(100L), eq(AdmissionStatus.PENDING), any()))
                .thenReturn(new PageImpl<>(content, PageRequest.of(1, 10), 21));

        AdmissionPageResponse result = service.getGlobalCurrentTermAdmissions(
                1, 10, AdmissionStatus.PENDING, "global@test.local"
        );

        assertEquals(10, result.content().size());
        assertEquals(1, result.page());
        assertEquals(10, result.size());
        assertEquals(21, result.totalElements());
        assertEquals(3, result.totalPages());
        assertFalse(result.first());
        assertFalse(result.last());
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(admissionRepository).findGlobalCurrentTermPage(eq(100L), eq(AdmissionStatus.PENDING), pageable.capture());
        assertEquals(10, pageable.getValue().getPageSize());
        assertTrue(pageable.getValue().getSort().getOrderFor("createdAt").isDescending());
        assertTrue(pageable.getValue().getSort().getOrderFor("id").isDescending());
    }

    @Test
    void selectedBulkApprovesEveryPendingAdmissionAndPreservesSideEffects() {
        authenticateGlobalAdmin();
        when(dormitoryTermRepository.findByActiveTrue()).thenReturn(Optional.of(activeTerm));
        Admission first = admission(1L, activeTerm, AdmissionStatus.PENDING);
        Admission second = admission(2L, activeTerm, AdmissionStatus.PENDING);
        when(admissionRepository.findAllById(any())).thenReturn(List.of(first, second));
        AppUser firstAdmin = dormitoryAdmin(50L, first.getDormitory());
        AppUser secondAdmin = dormitoryAdmin(51L, second.getDormitory());
        when(appUserRepository.findActiveDormitoryAdminsByDormitoryIds(any()))
                .thenReturn(List.of(firstAdmin, secondAdmin));

        var result = service.approveSelectedCurrentTermAdmissions(
                new BulkApproveAdmissionsRequest(List.of(1L, 2L)), "global@test.local"
        );

        assertEquals(2, result.approvedCount());
        assertEquals(AdmissionStatus.APPROVED, first.getStatus());
        assertEquals(AdmissionStatus.APPROVED, second.getStatus());
        verify(notificationService, times(2)).createNotification(anyLong(), any(), anyString(), anyString(), any(), anyLong());
        verify(notificationService, times(2)).createNotification(any(AppUser.class), any(), anyString(), anyString(), any(), anyLong());
        verify(appUserRepository, times(1)).findActiveDormitoryAdminsByDormitoryIds(any());
        verify(auditLogService, times(2)).recordStudentEvent(anyString(), any(), any(), any(), anyLong(), anyString(), any(), any(), anyString());
    }

    @Test
    void duplicateSelectedIdsAreProcessedOnce() {
        authenticateGlobalAdmin();
        when(dormitoryTermRepository.findByActiveTrue()).thenReturn(Optional.of(activeTerm));
        Admission admission = admission(1L, activeTerm, AdmissionStatus.PENDING);
        when(admissionRepository.findAllById(any())).thenReturn(List.of(admission));
        when(appUserRepository.findActiveDormitoryAdminsByDormitoryIds(any()))
                .thenReturn(List.of(dormitoryAdmin(50L, admission.getDormitory())));

        var result = service.approveSelectedCurrentTermAdmissions(
                new BulkApproveAdmissionsRequest(List.of(1L, 1L)), "global@test.local"
        );

        assertEquals(1, result.approvedCount());
        verify(notificationService, times(1)).createNotification(anyLong(), any(), anyString(), anyString(), any(), anyLong());
        verify(notificationService, times(1)).createNotification(any(AppUser.class), any(), anyString(), anyString(), any(), anyLong());
        verify(auditLogService, times(1)).recordStudentEvent(anyString(), any(), any(), any(), anyLong(), anyString(), any(), any(), anyString());
    }

    @Test
    void nonexistentSelectedIdFailsBeforeAnyApproval() {
        authenticateGlobalAdmin();
        when(dormitoryTermRepository.findByActiveTrue()).thenReturn(Optional.of(activeTerm));
        Admission existing = admission(1L, activeTerm, AdmissionStatus.PENDING);
        when(admissionRepository.findAllById(any())).thenReturn(List.of(existing));

        assertThrows(AdmissionNotFoundException.class, () -> service.approveSelectedCurrentTermAdmissions(
                new BulkApproveAdmissionsRequest(List.of(1L, 99L)), "global@test.local"
        ));
        assertEquals(AdmissionStatus.PENDING, existing.getStatus());
        verifyNoInteractions(notificationService, auditLogService);
    }

    @Test
    void nonPendingSelectedAdmissionFailsBeforeAnyApproval() {
        authenticateGlobalAdmin();
        when(dormitoryTermRepository.findByActiveTrue()).thenReturn(Optional.of(activeTerm));
        Admission pending = admission(1L, activeTerm, AdmissionStatus.PENDING);
        Admission approved = admission(2L, activeTerm, AdmissionStatus.APPROVED);
        when(admissionRepository.findAllById(any())).thenReturn(List.of(pending, approved));

        assertThrows(InvalidAdmissionStatusTransitionException.class, () -> service.approveSelectedCurrentTermAdmissions(
                new BulkApproveAdmissionsRequest(List.of(1L, 2L)), "global@test.local"
        ));
        assertEquals(AdmissionStatus.PENDING, pending.getStatus());
        verifyNoInteractions(notificationService, auditLogService);
    }

    @Test
    void previousTermAdmissionCannotBeBulkApproved() {
        authenticateGlobalAdmin();
        when(dormitoryTermRepository.findByActiveTrue()).thenReturn(Optional.of(activeTerm));
        Admission previous = admission(1L, term(90L, false), AdmissionStatus.PENDING);
        when(admissionRepository.findAllById(any())).thenReturn(List.of(previous));

        assertThrows(AdmissionAccessDeniedException.class, () -> service.approveSelectedCurrentTermAdmissions(
                new BulkApproveAdmissionsRequest(List.of(1L)), "global@test.local"
        ));
        assertEquals(AdmissionStatus.PENDING, previous.getStatus());
    }

    @Test
    void wrongRoleAndDormitoryScopeAreRejected() {
        AppUser student = user(8L, Role.STUDENT, null);
        when(appUserRepository.findByNormalizedEmail("student@test.local")).thenReturn(Optional.of(student));
        assertThrows(UserIsNotAdminException.class, () -> service.approveAllPendingCurrentTermAdmissions("student@test.local"));

        AppUser dormitoryAdmin = user(9L, Role.ADMIN, AdminScope.DORMITORY);
        when(appUserRepository.findByNormalizedEmail("dorm@test.local")).thenReturn(Optional.of(dormitoryAdmin));
        assertThrows(AccessDeniedException.class, () -> service.approveAllPendingCurrentTermAdmissions("dorm@test.local"));
        verifyNoInteractions(dormitoryTermRepository);
    }

    @Test
    void approveAllOnlyProcessesActiveTermPendingAdmissionsAndReturnsCount() {
        authenticateGlobalAdmin();
        when(dormitoryTermRepository.findByActiveTrue()).thenReturn(Optional.of(activeTerm));
        Admission first = admission(1L, activeTerm, AdmissionStatus.PENDING);
        Admission second = admission(2L, activeTerm, AdmissionStatus.PENDING);
        Admission approved = admission(3L, activeTerm, AdmissionStatus.APPROVED);
        Admission rejected = admission(4L, activeTerm, AdmissionStatus.REJECTED);
        Admission previousPending = admission(5L, term(90L, false), AdmissionStatus.PENDING);
        when(admissionRepository.findAllByTermIdAndStatus(100L, AdmissionStatus.PENDING))
                .thenReturn(List.of(first, second));
        when(appUserRepository.findActiveDormitoryAdminsByDormitoryIds(any()))
                .thenReturn(List.of(dormitoryAdmin(50L, first.getDormitory())));

        var result = service.approveAllPendingCurrentTermAdmissions("global@test.local");

        assertEquals(2, result.approvedCount());
        assertEquals(AdmissionStatus.APPROVED, first.getStatus());
        assertEquals(AdmissionStatus.APPROVED, second.getStatus());
        assertEquals(AdmissionStatus.APPROVED, approved.getStatus());
        assertEquals(AdmissionStatus.REJECTED, rejected.getStatus());
        assertEquals(AdmissionStatus.PENDING, previousPending.getStatus());
        verify(admissionRepository).findAllByTermIdAndStatus(100L, AdmissionStatus.PENDING);
        verify(notificationService, times(2)).createNotification(anyLong(), any(), anyString(), anyString(), any(), anyLong());
        verify(notificationService, times(1)).createNotification(any(AppUser.class), any(), anyString(), anyString(), any(), anyLong());
        verify(appUserRepository, times(1)).findActiveDormitoryAdminsByDormitoryIds(any());
    }

    @Test
    void singleGlobalApprovalKeepsStudentNotificationAndNotifiesEveryActiveDormitoryAdmin() {
        authenticateGlobalAdmin();
        Admission admission = admission(1L, activeTerm, AdmissionStatus.PENDING);
        when(admissionRepository.findById(1L)).thenReturn(Optional.of(admission));
        AppUser firstAdmin = dormitoryAdmin(50L, admission.getDormitory());
        AppUser secondAdmin = dormitoryAdmin(51L, admission.getDormitory());
        when(appUserRepository.findActiveDormitoryAdminsByDormitoryIds(any()))
                .thenReturn(List.of(firstAdmin, secondAdmin));

        service.updateStatus(1L, new com.ibb.yurtlar.dto.UpdateAdmissionStatusRequest(AdmissionStatus.APPROVED), "global@test.local");

        verify(notificationService).createNotification(
                eq(admission.getStudent().getUser().getId()), eq(com.ibb.yurtlar.enums.NotificationType.ADMISSION_APPROVED),
                anyString(), anyString(), eq(com.ibb.yurtlar.enums.NotificationReferenceType.ADMISSION), eq(1L)
        );
        verify(notificationService, times(2)).createNotification(
                any(AppUser.class), eq(com.ibb.yurtlar.enums.NotificationType.ADMISSION_APPROVED),
                eq("Yeni öğrenci kaydı"), eq("Öğrenci 1 yurdunuza öğrenci olarak kabul edildi."),
                eq(com.ibb.yurtlar.enums.NotificationReferenceType.ADMISSION), eq(1L)
        );
    }

    @Test
    void repositoryRecipientQueryDefinesScopeAndExcludesOtherUserCategories() throws Exception {
        var method = AppUserRepository.class.getMethod(
                "findActiveDormitoryAdminsByDormitoryIds", java.util.Collection.class
        );
        String query = method.getAnnotation(org.springframework.data.jpa.repository.Query.class).value();
        assertTrue(query.contains("Role.ADMIN"));
        assertTrue(query.contains("AdminScope.DORMITORY"));
        assertTrue(query.contains("admin.active = true"));
        assertTrue(query.contains("dormitory.id IN :dormitoryIds"));
    }

    @Test
    void previousTermSingleApprovalDoesNotNotifyDormitoryAdmins() {
        authenticateGlobalAdmin();
        Admission previous = admission(1L, term(90L, false), AdmissionStatus.PENDING);
        when(admissionRepository.findById(1L)).thenReturn(Optional.of(previous));

        service.updateStatus(1L, new com.ibb.yurtlar.dto.UpdateAdmissionStatusRequest(AdmissionStatus.APPROVED), "global@test.local");

        verify(notificationService).createNotification(anyLong(), any(), anyString(), anyString(), any(), anyLong());
        verify(notificationService, never()).createNotification(any(AppUser.class), any(), anyString(), anyString(), any(), anyLong());
        verify(appUserRepository, never()).findActiveDormitoryAdminsByDormitoryIds(any());
    }

    private void authenticateGlobalAdmin() {
        when(appUserRepository.findByNormalizedEmail("global@test.local"))
                .thenReturn(Optional.of(user(7L, Role.ADMIN, AdminScope.GLOBAL)));
    }

    private AppUser user(Long id, Role role, AdminScope scope) {
        AppUser user = new AppUser();
        user.setId(id); user.setFirstName("Global"); user.setLastName("Admin");
        user.setEmail("global@test.local"); user.setRole(role); user.setAdminScope(scope); user.setActive(true);
        return user;
    }

    private AppUser dormitoryAdmin(Long id, Dormitory dormitory) {
        AppUser admin = user(id, Role.ADMIN, AdminScope.DORMITORY);
        admin.setDormitory(dormitory);
        return admin;
    }

    private DormitoryTerm term(Long id, boolean active) {
        DormitoryTerm term = new DormitoryTerm();
        term.setId(id); term.setName("2026-2027"); term.setActive(active);
        return term;
    }

    private Admission admission(Long id, DormitoryTerm term, AdmissionStatus status) {
        AppUser studentUser = user(1000L + id, Role.STUDENT, null);
        studentUser.setFirstName("Öğrenci"); studentUser.setLastName(String.valueOf(id));
        Student student = new Student();
        student.setId(2000L + id); student.setIdentityNumber(String.format("%011d", id)); student.setUser(studentUser);
        Dormitory dormitory = new Dormitory();
        dormitory.setId(3000L + id); dormitory.setName("Yurt " + id);
        Admission admission = new Admission();
        admission.setId(id); admission.setStudent(student); admission.setDormitoryTerm(term);
        admission.setDormitory(dormitory); admission.setStatus(status);
        admission.setAdmissionDate(LocalDate.of(2026, 8, 1));
        admission.setCreatedAt(LocalDateTime.of(2026, 8, 1, 12, 0));
        admission.setUpdatedAt(LocalDateTime.of(2026, 8, 1, 12, 0));
        return admission;
    }
}
