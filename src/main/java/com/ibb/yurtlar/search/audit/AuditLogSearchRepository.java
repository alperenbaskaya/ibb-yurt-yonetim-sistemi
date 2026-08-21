package com.ibb.yurtlar.search.audit;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface AuditLogSearchRepository
        extends ElasticsearchRepository<AuditLogSearchDocument, String> {
}
