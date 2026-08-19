package com.ibb.yurtlar.search.audit;

import com.ibb.yurtlar.dto.AuditLogReindexResponse;
import com.ibb.yurtlar.entity.AuditLog;
import com.ibb.yurtlar.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import com.ibb.yurtlar.observability.ElasticsearchMetricsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuditLogReindexServiceTest {
    private final AuditLogRepository mysql = mock(AuditLogRepository.class);
    private final ElasticsearchOperations elasticsearch = mock(ElasticsearchOperations.class);
    private final AuditLogIndexMapper mapper = mock(AuditLogIndexMapper.class);
    private final ElasticsearchMetricsService metrics = mock(ElasticsearchMetricsService.class);
    private final AuditLogReindexService service =
            new AuditLogReindexService(mysql, elasticsearch, mapper, 2, metrics);

    @Test
    void usesKeysetBatchesBoundedByCapturedHighWaterMark() {
        AuditLog one = audit(1L); AuditLog two = audit(2L); AuditLog four = audit(4L);
        when(mysql.findHighWaterMark()).thenReturn(4L);
        when(mysql.findReindexBatch(eq(0L), eq(4L), any(Pageable.class))).thenReturn(List.of(one, two));
        when(mysql.findReindexBatch(eq(2L), eq(4L), any(Pageable.class))).thenReturn(List.of(four));
        when(mysql.findReindexBatch(eq(4L), eq(4L), any(Pageable.class))).thenReturn(List.of());
        when(mapper.toDocument(any(AuditLog.class))).thenAnswer(i -> document(i.getArgument(0)));

        AuditLogReindexResponse result = service.reindex();

        assertThat(result).isEqualTo(new AuditLogReindexResponse(4, 3, 3, 4));
        verify(mysql).findReindexBatch(eq(0L), eq(4L), argThat(p -> p.getPageSize() == 2));
        verify(mysql).findReindexBatch(eq(2L), eq(4L), argThat(p -> p.getPageSize() == 2));
        verify(elasticsearch, times(2)).save(any(Iterable.class));
        verify(metrics).reindexSuccess();
    }

    @Test
    void rerunIndexesTheSameDeterministicDocumentIds() {
        AuditLog one = audit(1L);
        when(mysql.findHighWaterMark()).thenReturn(1L);
        when(mysql.findReindexBatch(eq(0L), eq(1L), any())).thenReturn(List.of(one));
        when(mapper.toDocument(one)).thenReturn(document(one));

        service.reindex();
        service.reindex();

        verify(elasticsearch, times(2)).save(argThat((Iterable<AuditLogSearchDocument> values) ->
                ((List<AuditLogSearchDocument>) values).getFirst().getId().equals("1")));
    }

    @Test
    void failedBatchReturnsStableUnavailableErrorWithoutDeletingAnything() {
        AuditLog one = audit(1L);
        when(mysql.findHighWaterMark()).thenReturn(1L);
        when(mysql.findReindexBatch(eq(0L), eq(1L), any())).thenReturn(List.of(one));
        when(mapper.toDocument(one)).thenReturn(document(one));
        when(elasticsearch.save(any(Iterable.class)))
                .thenThrow(new RuntimeException("cluster unavailable"));

        assertThatThrownBy(service::reindex)
                .isInstanceOf(com.ibb.yurtlar.exception.BusinessException.class)
                .extracting("code").isEqualTo("AUDIT_SEARCH_UNAVAILABLE");
        verify(elasticsearch, never()).delete(any(AuditLogSearchDocument.class));
        verify(mysql, never()).save(any());
        verify(metrics).reindexFailure();
    }

    private AuditLog audit(long id) {
        AuditLog audit = mock(AuditLog.class);
        when(audit.getId()).thenReturn(id);
        return audit;
    }

    private AuditLogSearchDocument document(AuditLog audit) {
        return mock(AuditLogSearchDocument.class, invocation ->
                invocation.getMethod().getName().equals("getId")
                        ? audit.getId().toString() : RETURNS_DEFAULTS.answer(invocation));
    }
}
