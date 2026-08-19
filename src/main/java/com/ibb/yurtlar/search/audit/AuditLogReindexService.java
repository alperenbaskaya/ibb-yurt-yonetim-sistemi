package com.ibb.yurtlar.search.audit;

import com.ibb.yurtlar.dto.AuditLogReindexResponse;
import com.ibb.yurtlar.entity.AuditLog;
import com.ibb.yurtlar.exception.BusinessException;
import com.ibb.yurtlar.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ibb.yurtlar.exception.reason.BusinessExceptionReason.AUDIT_SEARCH_UNAVAILABLE;

@Service
public class AuditLogReindexService {
    private static final Logger log = LoggerFactory.getLogger(AuditLogReindexService.class);

    private final AuditLogRepository auditLogRepository;
    private final ElasticsearchOperations operations;
    private final AuditLogIndexMapper mapper;
    private final int batchSize;

    public AuditLogReindexService(
            AuditLogRepository auditLogRepository,
            ElasticsearchOperations operations,
            AuditLogIndexMapper mapper,
            @Value("${app.elasticsearch.audit-reindex-batch-size:500}") int batchSize
    ) {
        this.auditLogRepository = auditLogRepository;
        this.operations = operations;
        this.mapper = mapper;
        this.batchSize = batchSize;
    }

    @Transactional(readOnly = true)
    public AuditLogReindexResponse reindex() {
        long highWaterMark = auditLogRepository.findHighWaterMark();
        long lastProcessedId = 0;
        long scanned = 0;
        long indexed = 0;

        try {
            while (lastProcessedId < highWaterMark) {
                List<AuditLog> batch = auditLogRepository.findReindexBatch(
                        lastProcessedId, highWaterMark, PageRequest.of(0, batchSize));
                if (batch.isEmpty()) {
                    break;
                }
                List<AuditLogSearchDocument> documents = batch.stream()
                        .map(mapper::toDocument)
                        .toList();
                operations.save(documents);
                scanned += batch.size();
                indexed += documents.size();
                lastProcessedId = batch.getLast().getId();
            }
            return new AuditLogReindexResponse(
                    highWaterMark, scanned, indexed, lastProcessedId);
        } catch (RuntimeException exception) {
            log.error("Audit log Elasticsearch reindex failed. highWaterMark={}, lastProcessedId={}",
                    highWaterMark, lastProcessedId, exception);
            throw new BusinessException(AUDIT_SEARCH_UNAVAILABLE);
        }
    }
}
