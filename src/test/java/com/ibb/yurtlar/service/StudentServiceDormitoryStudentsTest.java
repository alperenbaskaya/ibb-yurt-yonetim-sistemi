package com.ibb.yurtlar.service;

import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Dormitory;
import com.ibb.yurtlar.entity.DormitoryTerm;
import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.exception.InvalidAdminConfigurationException;
import com.ibb.yurtlar.exception.UserIsNotAdminException;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.DormitoryTermRepository;
import com.ibb.yurtlar.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceDormitoryStudentsTest {

    @Mock StudentRepository studentRepository;
    @Mock AppUserRepository appUserRepository;
    @Mock DormitoryTermRepository dormitoryTermRepository;

    @InjectMocks StudentService service;

    @Test
    void dormitoryAdminUsesOwnDormitoryAndServerResolvedActiveTermApprovedQuery() {
        AppUser admin = dormitoryAdmin(10L);
        DormitoryTerm activeTerm = new DormitoryTerm();
        activeTerm.setId(20L);
        when(appUserRepository.findByNormalizedEmail("admin@example.com"))
                .thenReturn(Optional.of(admin));
        when(dormitoryTermRepository.findByActiveTrue())
                .thenReturn(Optional.of(activeTerm));
        when(studentRepository.findApprovedStudentsByDormitoryAndTerm(10L, 20L))
                .thenReturn(List.of());

        service.getAll("admin@example.com");

        verify(studentRepository)
                .findApprovedStudentsByDormitoryAndTerm(10L, 20L);
    }

    @Test
    void globalAdminListingRemainsGlobalAndDoesNotUseDormitoryParticipationQuery() {
        AppUser admin = new AppUser();
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin.setAdminScope(AdminScope.GLOBAL);
        when(appUserRepository.findByNormalizedEmail("global@example.com"))
                .thenReturn(Optional.of(admin));
        when(studentRepository.findAll()).thenReturn(List.of());

        service.getAll("global@example.com");

        verify(studentRepository).findAll();
        verify(studentRepository, never())
                .findApprovedStudentsByDormitoryAndTerm(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.anyLong()
                );
    }

    @Test
    void wrongRoleRemainsRejected() {
        AppUser reviewer = new AppUser();
        reviewer.setId(30L);
        reviewer.setRole(Role.REVIEWER);
        reviewer.setActive(true);
        when(appUserRepository.findByNormalizedEmail("reviewer@example.com"))
                .thenReturn(Optional.of(reviewer));

        assertThrows(
                UserIsNotAdminException.class,
                () -> service.getAll("reviewer@example.com")
        );
    }

    @Test
    void dormitoryScopeWithoutAssignmentRemainsRejected() {
        AppUser admin = dormitoryAdmin(null);
        when(appUserRepository.findByNormalizedEmail("admin@example.com"))
                .thenReturn(Optional.of(admin));

        assertThrows(
                InvalidAdminConfigurationException.class,
                () -> service.getAll("admin@example.com")
        );
    }

    private AppUser dormitoryAdmin(Long dormitoryId) {
        AppUser admin = new AppUser();
        admin.setRole(Role.ADMIN);
        admin.setActive(true);
        admin.setAdminScope(AdminScope.DORMITORY);

        if (dormitoryId != null) {
            Dormitory dormitory = new Dormitory();
            dormitory.setId(dormitoryId);
            admin.setDormitory(dormitory);
        }

        return admin;
    }
}
