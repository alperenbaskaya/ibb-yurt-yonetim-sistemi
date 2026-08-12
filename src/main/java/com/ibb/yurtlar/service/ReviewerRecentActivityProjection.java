package com.ibb.yurtlar.service;

import com.ibb.yurtlar.dto.AuditLogResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class ReviewerRecentActivityProjection {
    private static final Logger log = LoggerFactory.getLogger(ReviewerRecentActivityProjection.class);
    private static final int MAX_ENTRIES = 20;
    private static final double SORT_SCORE = 0D;
    private static final DateTimeFormatter MEMBER_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSSSSS");
    private static final DefaultRedisScript<Long> TRIM_SCRIPT = new DefaultRedisScript<>("""
            local size = redis.call('ZCARD', KEYS[1])
            local maximum = tonumber(ARGV[1])
            if size <= maximum then
                return 0
            end
            local overflow = size - maximum
            local oldest = redis.call('ZRANGE', KEYS[1], 0, overflow - 1)
            for _, member in ipairs(oldest) do
                redis.call('ZREM', KEYS[1], member)
                redis.call('HDEL', KEYS[2], member)
            end
            return #oldest
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public ReviewerRecentActivityProjection(StringRedisTemplate redisTemplate,
                                            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public Optional<List<AuditLogResponse>> readRecent(Long dormitoryId, int limit) {
        try {
            String key = key(dormitoryId);
            Set<String> members = redisTemplate.opsForZSet()
                    .reverseRange(key, 0, limit - 1L);
            if (members == null) {
                return Optional.empty();
            }
            if (members.isEmpty()) {
                return Boolean.TRUE.equals(redisTemplate.hasKey(initializedKey(key)))
                        ? Optional.of(List.of()) : Optional.empty();
            }

            List<Object> values = redisTemplate.opsForHash()
                    .multiGet(valuesKey(key), new ArrayList<>(members));
            if (values.size() != members.size() || values.stream().anyMatch(value -> value == null)) {
                return Optional.empty();
            }

            List<AuditLogResponse> activities = new ArrayList<>(values.size());
            for (Object value : values) {
                activities.add(objectMapper.readValue((String) value, AuditLogResponse.class));
            }
            return Optional.of(List.copyOf(activities));
        } catch (Exception exception) {
            log.warn("Reviewer recent activity Redis read failed for dormitory {}", dormitoryId, exception);
            return Optional.empty();
        }
    }

    public void add(AuditLogResponse activity) {
        try {
            String key = key(activity.dormitoryId());
            merge(key, List.of(activity));
            trimToNewest(key);
            markInitialized(key);
        } catch (Exception exception) {
            log.warn("Reviewer recent activity Redis update failed for dormitory {}",
                    activity.dormitoryId(), exception);
        }
    }

    public void mergeAndInitialize(Long dormitoryId, List<AuditLogResponse> activities) {
        try {
            String key = key(dormitoryId);
            merge(key, activities);
            trimToNewest(key);
            markInitialized(key);
        } catch (Exception exception) {
            log.warn("Reviewer recent activity Redis warm failed for dormitory {}", dormitoryId, exception);
        }
    }

    private void merge(String key, List<AuditLogResponse> activities) {
        for (AuditLogResponse activity : activities) {
            String member = member(activity);
            redisTemplate.opsForHash().put(valuesKey(key), member, serialize(activity));
            redisTemplate.opsForZSet().add(key, member, SORT_SCORE);
        }
    }

    private void trimToNewest(String key) {
        redisTemplate.execute(
                TRIM_SCRIPT,
                List.of(key, valuesKey(key)),
                Integer.toString(MAX_ENTRIES)
        );
    }

    private void markInitialized(String key) {
        redisTemplate.opsForValue().set(initializedKey(key), "1");
    }

    private String serialize(AuditLogResponse activity) {
        try {
            return objectMapper.writeValueAsString(activity);
        } catch (Exception exception) {
            throw new IllegalStateException("Reviewer activity could not be serialized", exception);
        }
    }

    private String member(AuditLogResponse activity) {
        return MEMBER_TIME_FORMAT.format(activity.createdAt())
                + ":" + String.format("%019d", activity.id());
    }

    private String key(Long dormitoryId) {
        return "reviewer-activity:dormitory:" + dormitoryId + ":recent";
    }

    private String valuesKey(String key) {
        return key + ":values";
    }

    private String initializedKey(String key) {
        return key + ":initialized";
    }
}
