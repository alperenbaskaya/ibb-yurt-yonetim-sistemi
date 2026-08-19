package com.ibb.yurtlar.search.audit;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ibb.yurtlar.dto.AuditLogAnalyticsResponse;
import com.ibb.yurtlar.dto.AuditLogPageResponse;
import com.ibb.yurtlar.dto.AuditLogResponse;
import com.ibb.yurtlar.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.AUDIT_SEARCH_UNAVAILABLE;
import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.INVALID_AUDIT_HISTORY_REQUEST;

@Service
public class AuditLogSearchService {
    private static final Logger log = LoggerFactory.getLogger(AuditLogSearchService.class);
    private static final String[] TEXT_FIELDS = {
            "actorName", "subjectStudentName", "dormitoryName", "targetLabel", "description"
    };

    private final ElasticsearchOperations operations;

    public AuditLogSearchService(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    public AuditLogPageResponse search(AuditLogSearchCriteria criteria, Long authorizedDormitoryId) {
        validate(criteria.page(), criteria.size(), criteria.from(), criteria.to());
        try {
            SearchHits<AuditLogSearchDocument> hits = operations.search(
                    buildSearchQuery(criteria, authorizedDormitoryId), AuditLogSearchDocument.class);
            long total = hits.getTotalHits();
            int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / criteria.size());
            return new AuditLogPageResponse(
                    hits.getSearchHits().stream().map(hit -> toResponse(hit.getContent())).toList(),
                    criteria.page(), criteria.size(), total, totalPages,
                    criteria.page() == 0, criteria.page() + 1 >= totalPages);
        } catch (RuntimeException exception) {
            throw unavailable("search", exception);
        }
    }

    public AuditLogAnalyticsResponse analytics(LocalDateTime from, LocalDateTime to) {
        validate(0, 1, from, to);
        try {
            NativeQueryBuilder builder = new NativeQueryBuilder()
                    .withQuery(buildFilterQuery(null, null, null, null, null, null,
                            null, null, null, from, to))
                    .withPageable(PageRequest.of(0, 1))
                    .withMaxResults(0)
                    .withAggregation("by_action", terms("action", 100))
                    .withAggregation("by_category", terms("category", 100))
                    .withAggregation("by_dormitory", terms("dormitoryId", 1000))
                    .withAggregation("activity_by_day", Aggregation.of(a -> a.dateHistogram(h -> h
                            .field("createdAt").calendarInterval(co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval.Day)
                            .format("yyyy-MM-dd").minDocCount(0))));
            SearchHits<AuditLogSearchDocument> hits = operations.search(
                    builder.build(), AuditLogSearchDocument.class);
            ElasticsearchAggregations aggregations =
                    (ElasticsearchAggregations) hits.getAggregations();
            return mapAnalytics(aggregations);
        } catch (RuntimeException exception) {
            throw unavailable("analytics", exception);
        }
    }

    NativeQuery buildSearchQuery(AuditLogSearchCriteria criteria, Long authorizedDormitoryId) {
        Long dormitoryId = authorizedDormitoryId != null
                ? authorizedDormitoryId : criteria.dormitoryId();
        return new NativeQueryBuilder()
                .withQuery(buildFilterQuery(criteria.q(), criteria.category(), criteria.action(),
                        criteria.actorRole(), criteria.actorUserId(), criteria.subjectStudentId(),
                        dormitoryId, criteria.entityType(), criteria.entityId(),
                        criteria.from(), criteria.to()))
                .withPageable(PageRequest.of(criteria.page(), criteria.size()))
                .withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("auditLogId").order(SortOrder.Desc)))
                .build();
    }

    private Query buildFilterQuery(String q, Object category, Object action, Object actorRole,
                                   Long actorUserId, Long subjectStudentId, Long dormitoryId,
                                   Object entityType, Long entityId,
                                   LocalDateTime from, LocalDateTime to) {
        List<Query> must = new ArrayList<>();
        List<Query> filter = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            must.add(Query.of(query -> query.multiMatch(m -> m
                    .query(q.trim()).fields(List.of(TEXT_FIELDS)))));
        }
        addTerm(filter, "category", category);
        addTerm(filter, "action", action);
        addTerm(filter, "actorRole", actorRole);
        addTerm(filter, "actorUserId", actorUserId);
        addTerm(filter, "subjectStudentId", subjectStudentId);
        addTerm(filter, "dormitoryId", dormitoryId);
        addTerm(filter, "entityType", entityType);
        addTerm(filter, "entityId", entityId);
        if (from != null || to != null) {
            filter.add(Query.of(query -> query.range(range -> range.date(date -> {
                date.field("createdAt");
                if (from != null) date.gte(from.toString());
                if (to != null) date.lte(to.toString());
                return date;
            }))));
        }
        return Query.of(query -> query.bool(bool -> bool.must(must).filter(filter)));
    }

    private void addTerm(List<Query> filters, String field, Object value) {
        if (value == null) return;
        filters.add(Query.of(query -> query.term(term -> {
            term.field(field);
            if (value instanceof Long number) {
                term.value(number);
            } else {
                term.value(value instanceof Enum<?> e ? e.name() : value.toString());
            }
            return term;
        })));
    }

    private Aggregation terms(String field, int size) {
        return Aggregation.of(a -> a.terms(t -> t.field(field).size(size)));
    }

    AuditLogAnalyticsResponse mapAnalytics(ElasticsearchAggregations aggregations) {
        Aggregate actions = aggregations.get("by_action").aggregation().getAggregate();
        Aggregate categories = aggregations.get("by_category").aggregation().getAggregate();
        Aggregate dormitories = aggregations.get("by_dormitory").aggregation().getAggregate();
        Aggregate days = aggregations.get("activity_by_day").aggregation().getAggregate();
        return new AuditLogAnalyticsResponse(
                actions.sterms().buckets().array().stream()
                        .map(b -> new AuditLogAnalyticsResponse.Bucket(b.key().stringValue(), b.docCount())).toList(),
                categories.sterms().buckets().array().stream()
                        .map(b -> new AuditLogAnalyticsResponse.Bucket(b.key().stringValue(), b.docCount())).toList(),
                dormitories.lterms().buckets().array().stream()
                        .map(b -> new AuditLogAnalyticsResponse.Bucket(Long.toString(b.key()), b.docCount())).toList(),
                days.dateHistogram().buckets().array().stream()
                        .map(b -> new AuditLogAnalyticsResponse.DailyBucket(
                                LocalDate.parse(b.keyAsString()), b.docCount())).toList());
    }

    private void validate(int page, int size, LocalDateTime from, LocalDateTime to) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BusinessException(INVALID_AUDIT_HISTORY_REQUEST,
                    "Sayfa 0 veya daha büyük, sayfa boyutu 1 ile 100 arasında olmalıdır.");
        }
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException(INVALID_AUDIT_HISTORY_REQUEST,
                    "Başlangıç tarihi bitiş tarihinden sonra olamaz.");
        }
    }

    private AuditLogResponse toResponse(AuditLogSearchDocument document) {
        return new AuditLogResponse(document.getAuditLogId(), document.getActorUserId(),
                document.getActorName(), document.getActorRole(), document.getSubjectStudentId(),
                document.getSubjectStudentName(), document.getDormitoryId(), document.getDormitoryName(),
                document.getCategory(), document.getAction(), document.getEntityType(), document.getEntityId(),
                document.getTargetLabel(), document.getDescription(), document.getCreatedAt());
    }

    private BusinessException unavailable(String operation, RuntimeException exception) {
        log.error("Audit log Elasticsearch {} failed", operation, exception);
        return new BusinessException(AUDIT_SEARCH_UNAVAILABLE);
    }
}
