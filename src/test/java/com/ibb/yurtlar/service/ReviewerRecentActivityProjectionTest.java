package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.AuditLogResponse;
import com.ibb.yurtlar.enums.AuditAction;
import com.ibb.yurtlar.enums.AuditCategory;
import com.ibb.yurtlar.enums.AuditEntityType;
import com.ibb.yurtlar.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.RedisScript;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewerRecentActivityProjectionTest {
    private static final String KEY = "reviewer-activity:dormitory:10:recent";

    @Mock StringRedisTemplate redisTemplate;
    @Mock ZSetOperations<String, String> zSetOperations;
    @Mock HashOperations<String, Object, Object> hashOperations;
    @Mock ValueOperations<String, String> valueOperations;
    @Mock ObjectMapper objectMapper;

    private ReviewerRecentActivityProjection projection;

    @BeforeEach
    void setUp() {
        projection = new ReviewerRecentActivityProjection(redisTemplate, objectMapper);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    void readsInCreatedAtThenIdDescendingOrderIndependentOfInsertionArrival() throws Exception {
        AuditLogResponse older = response(8L, LocalDateTime.of(2026, 8, 12, 10, 0, 0));
        AuditLogResponse newerLowId = response(9L, LocalDateTime.of(2026, 8, 12, 10, 0, 1));
        AuditLogResponse newerHighId = response(10L, LocalDateTime.of(2026, 8, 12, 10, 0, 1));
        Set<String> orderedMembers = new LinkedHashSet<>(List.of(
                "20260812100001000000000:0000000000000000010",
                "20260812100001000000000:0000000000000000009",
                "20260812100000000000000:0000000000000000008"
        ));
        when(zSetOperations.reverseRange(KEY, 0, 4)).thenReturn(orderedMembers);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.multiGet(KEY + ":values", List.copyOf(orderedMembers)))
                .thenReturn(List.of("high", "low", "old"));
        when(objectMapper.readValue("high", AuditLogResponse.class)).thenReturn(newerHighId);
        when(objectMapper.readValue("low", AuditLogResponse.class)).thenReturn(newerLowId);
        when(objectMapper.readValue("old", AuditLogResponse.class)).thenReturn(older);

        assertEquals(Optional.of(List.of(newerHighId, newerLowId, older)),
                projection.readRecent(10L, 5));
    }

    @Test
    void warmingMergesWithoutDeletingAConcurrentNewerEntry() throws Exception {
        AuditLogResponse older = response(8L, LocalDateTime.of(2026, 8, 12, 10, 0));
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(older)).thenReturn("json");

        projection.mergeAndInitialize(10L, List.of(older));

        verify(redisTemplate, never()).delete(anyString());
        verify(zSetOperations).add(KEY,
                "20260812100000000000000:0000000000000000008", 0D);
        verify(redisTemplate).execute(any(RedisScript.class),
                eq(List.of(KEY, KEY + ":values")), eq("20"));
    }

    @Test
    void sameAuditEntryUsesTheSameUniqueZsetMember() throws Exception {
        AuditLogResponse activity = response(8L, LocalDateTime.of(2026, 8, 12, 10, 0));
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(activity)).thenReturn("json");

        projection.mergeAndInitialize(10L, List.of(activity, activity));

        ArgumentCaptor<String> memberCaptor = ArgumentCaptor.forClass(String.class);
        verify(zSetOperations, org.mockito.Mockito.times(2))
                .add(anyString(), memberCaptor.capture(), anyDouble());
        assertEquals(memberCaptor.getAllValues().get(0), memberCaptor.getAllValues().get(1));
    }

    @Test
    void atomicTrimDoesNothingWhenCurrentCardinalityIsAtMostTwenty() throws Exception {
        AuditLogResponse activity = response(20L, LocalDateTime.of(2026, 8, 12, 10, 0));
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(activity)).thenReturn("json");
        when(redisTemplate.execute(any(RedisScript.class), any(), anyString()))
                .thenReturn(0L);

        projection.add(activity);

        verify(redisTemplate).execute(any(RedisScript.class),
                eq(List.of(KEY, KEY + ":values")), eq("20"));
        verify(zSetOperations, never()).remove(anyString(), any(Object[].class));
        verify(hashOperations, never()).delete(anyString(), any(Object[].class));
    }

    @Test
    void atomicTrimScriptRemovesCurrentOldestOverflowAndMatchingHashFields() throws Exception {
        AuditLogResponse activity = response(23L, LocalDateTime.of(2026, 8, 12, 10, 0));
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(activity)).thenReturn("json");
        when(redisTemplate.execute(any(RedisScript.class), any(), anyString()))
                .thenReturn(3L);

        projection.add(activity);

        ArgumentCaptor<RedisScript<Long>> scriptCaptor = ArgumentCaptor.forClass(RedisScript.class);
        verify(redisTemplate).execute(scriptCaptor.capture(),
                eq(List.of(KEY, KEY + ":values")), eq("20"));
        String script = scriptCaptor.getValue().getScriptAsString();
        assertTrue(script.contains("local overflow = size - maximum"));
        assertTrue(script.contains("'ZRANGE', KEYS[1], 0, overflow - 1"));
        assertTrue(script.contains("'ZREM', KEYS[1], member"));
        assertTrue(script.contains("'HDEL', KEYS[2], member"));
    }

    @Test
    void initializedEmptyProjectionIsARedisHit() {
        when(zSetOperations.reverseRange(KEY, 0, 4)).thenReturn(Set.of());
        when(redisTemplate.hasKey(KEY + ":initialized")).thenReturn(true);

        Optional<List<AuditLogResponse>> result = projection.readRecent(10L, 5);

        assertTrue(result.isPresent());
        assertTrue(result.get().isEmpty());
    }

    private AuditLogResponse response(Long id, LocalDateTime createdAt) {
        return new AuditLogResponse(id, 11L, "Student One", Role.STUDENT,
                12L, "Student One", 10L, "Dormitory", AuditCategory.STUDENT_ACTIVITY,
                AuditAction.DOCUMENT_UPLOADED, AuditEntityType.STUDENT_DOCUMENT,
                13L, "Identity Document", "uploaded", createdAt);
    }
}
