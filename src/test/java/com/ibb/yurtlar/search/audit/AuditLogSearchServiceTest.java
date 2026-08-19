package com.ibb.yurtlar.search.audit;

import com.ibb.yurtlar.enums.*;
import com.ibb.yurtlar.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditLogSearchServiceTest {
    private final ElasticsearchOperations operations = mock(ElasticsearchOperations.class);
    private final AuditLogSearchService service = new AuditLogSearchService(operations);

    @Test
    void buildsTextExactDateFiltersAndDeterministicSorting() {
        AuditLogSearchCriteria criteria = new AuditLogSearchCriteria("student name",
                AuditCategory.STUDENT_ACTIVITY, AuditAction.DOCUMENT_APPROVED,
                Role.REVIEWER, 10L, 20L, 30L, AuditEntityType.STUDENT_DOCUMENT, 40L,
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 8, 19, 23, 59), 2, 25);

        NativeQuery query = service.buildSearchQuery(criteria, null);
        String json = query.getQuery().toString();

        assertThat(json).contains("multi_match", "actorName", "subjectStudentName",
                "dormitoryName", "targetLabel", "description", "category",
                "STUDENT_ACTIVITY", "actorUserId", "10", "createdAt");
        assertThat(query.getSortOptions()).hasSize(2);
        assertThat(query.getSortOptions().get(0).field().field()).isEqualTo("createdAt");
        assertThat(query.getSortOptions().get(1).field().field()).isEqualTo("auditLogId");
        assertThat(query.getPageable().getPageNumber()).isEqualTo(2);
    }

    @Test
    void authorizedDormitoryOverridesAnyClientDormitory() {
        AuditLogSearchCriteria criteria = criteria(999L);

        String json = service.buildSearchQuery(criteria, 55L).getQuery().toString();

        assertThat(json).contains("dormitoryId", "55").doesNotContain("999");
    }

    @Test
    void rejectsInvalidRangeBeforeCallingElasticsearch() {
        AuditLogSearchCriteria criteria = new AuditLogSearchCriteria(null, null, null, null,
                null, null, null, null, null,
                LocalDateTime.of(2026, 8, 20, 0, 0),
                LocalDateTime.of(2026, 8, 19, 0, 0), 0, 20);
        assertThatThrownBy(() -> service.search(criteria, null))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("INVALID_AUDIT_HISTORY_REQUEST");
        verifyNoInteractions(operations);
    }

    @Test
    void translatesElasticsearchFailureToStableServiceUnavailable() {
        when(operations.search(any(NativeQuery.class), eq(AuditLogSearchDocument.class)))
                .thenThrow(new RuntimeException("connection refused"));

        assertThatThrownBy(() -> service.search(criteria(null), null))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getCode()).isEqualTo("AUDIT_SEARCH_UNAVAILABLE");
                    assertThat(businessException.getHttpStatus().value()).isEqualTo(503);
                    assertThat(businessException.getMessage()).doesNotContain("connection refused");
                });
    }

    @Test
    void mapsAggregationBucketsToCompactAnalyticsResponse() {
        Aggregate action = Aggregate.of(a -> a.sterms(s -> s.buckets(b -> b.array(java.util.List.of(
                co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket.of(
                        bucket -> bucket.key("DOCUMENT_APPROVED").docCount(3)))))));
        Aggregate category = Aggregate.of(a -> a.sterms(s -> s.buckets(b -> b.array(java.util.List.of(
                co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket.of(
                        bucket -> bucket.key("STUDENT_ACTIVITY").docCount(4)))))));
        Aggregate dormitory = Aggregate.of(a -> a.lterms(s -> s.buckets(b -> b.array(java.util.List.of(
                co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket.of(
                        bucket -> bucket.key(55).docCount(2)))))));
        Aggregate day = Aggregate.of(a -> a.dateHistogram(h -> h.buckets(b -> b.array(java.util.List.of(
                co.elastic.clients.elasticsearch._types.aggregations.DateHistogramBucket.of(
                        bucket -> bucket.key(1787011200000L)
                                .keyAsString("2026-08-18").docCount(5)))))));
        ElasticsearchAggregations aggregations = new ElasticsearchAggregations(Map.of(
                "by_action", action,
                "by_category", category,
                "by_dormitory", dormitory,
                "activity_by_day", day
        ));

        var response = service.mapAnalytics(aggregations);

        assertThat(response.byAction().getFirst().key()).isEqualTo("DOCUMENT_APPROVED");
        assertThat(response.byDormitory().getFirst().key()).isEqualTo("55");
        assertThat(response.activityByDay().getFirst().date()).isEqualTo(LocalDate.of(2026, 8, 18));
        assertThat(response.activityByDay().getFirst().count()).isEqualTo(5);
    }

    private AuditLogSearchCriteria criteria(Long dormitoryId) {
        return new AuditLogSearchCriteria(null, null, null, null, null, null,
                dormitoryId, null, null, null, null, 0, 20);
    }
}
