package com.ibb.yurtlar.service;

import com.ibb.yurtlar.entity.AppUser;
import com.ibb.yurtlar.entity.Notification;
import com.ibb.yurtlar.enums.NotificationType;
import com.ibb.yurtlar.repository.AppUserRepository;
import com.ibb.yurtlar.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServicePaginationTest {
    @Mock NotificationRepository notificationRepository;
    @Mock AppUserRepository appUserRepository;
    private NotificationService service;
    private AppUser authenticatedUser;

    @BeforeEach
    void setUp() {
        service = new NotificationService(notificationRepository, appUserRepository);
        authenticatedUser = new AppUser();
        authenticatedUser.setId(42L);
        lenient().when(appUserRepository.findByNormalizedEmail("user@test.local"))
                .thenReturn(Optional.of(authenticatedUser));
    }

    @Test
    void notificationPageReturnsRequestedSizeAndMetadataForAuthenticatedUserOnly() {
        List<Notification> content = java.util.stream.LongStream.rangeClosed(11, 20)
                .mapToObj(id -> notification(id, authenticatedUser, false)).toList();
        when(notificationRepository.findAllForRecipient(eq(42L), any()))
                .thenReturn(new PageImpl<>(content, PageRequest.of(1, 10), 23));

        var result = service.getMyNotifications("user@test.local", 1, 10);

        assertEquals(10, result.content().size());
        assertEquals(1, result.page());
        assertEquals(10, result.size());
        assertEquals(23, result.totalElements());
        assertEquals(3, result.totalPages());
        assertFalse(result.first());
        assertFalse(result.last());
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(notificationRepository).findAllForRecipient(eq(42L), pageable.capture());
        assertEquals(10, pageable.getValue().getPageSize());
    }

    @Test
    void unreadNotificationsUseServerSidePageForAuthenticatedUser() {
        when(notificationRepository.findUnreadForRecipient(eq(42L), any()))
                .thenReturn(new PageImpl<>(List.of(notification(5L, authenticatedUser, false)), PageRequest.of(0, 10), 1));

        var result = service.getMyUnreadNotifications("user@test.local", 0, 10);

        assertEquals(1, result.totalElements());
        assertFalse(result.content().getFirst().read());
        verify(notificationRepository).findUnreadForRecipient(eq(42L), any(Pageable.class));
    }

    @Test
    void unreadCountKeepsScalarSemanticsForAuthenticatedUser() {
        when(notificationRepository.countUnreadForRecipient(42L)).thenReturn(17L);
        assertEquals(17L, service.getMyUnreadCount("user@test.local").unreadCount());
    }

    @Test
    void repositoryQueriesUseDeterministicCreatedAtAndIdOrdering() throws Exception {
        for (String methodName : List.of("findAllForRecipient", "findUnreadForRecipient")) {
            var method = NotificationRepository.class.getMethod(methodName, Long.class, Pageable.class);
            String query = method.getAnnotation(org.springframework.data.jpa.repository.Query.class).value();
            assertTrue(query.contains("notification.createdAt DESC, notification.id DESC"));
        }
    }

    private Notification notification(Long id, AppUser recipient, boolean read) {
        Notification notification = new Notification();
        notification.setId(id); notification.setRecipient(recipient);
        notification.setType(NotificationType.ADMISSION_APPROVED);
        notification.setTitle("Başlık"); notification.setMessage("Mesaj");
        notification.setRead(read); notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
}
