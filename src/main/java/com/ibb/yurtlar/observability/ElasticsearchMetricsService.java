package com.ibb.yurtlar.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ElasticsearchMetricsService {
    private static final Logger log = LoggerFactory.getLogger(ElasticsearchMetricsService.class);
    private final MeterRegistry registry;

    public ElasticsearchMetricsService(MeterRegistry registry) {
        this.registry = registry;
    }

    public void indexSuccess() { increment("yurtlar_elasticsearch_index_success_total"); }
    public void indexFailure() { increment("yurtlar_elasticsearch_index_failure_total"); }
    public void searchSuccess() { increment("yurtlar_elasticsearch_search_success_total"); }
    public void searchFailure() { increment("yurtlar_elasticsearch_search_failure_total"); }
    public void reindexSuccess() { increment("yurtlar_elasticsearch_reindex_success_total"); }
    public void reindexFailure() { increment("yurtlar_elasticsearch_reindex_failure_total"); }

    private void increment(String name) {
        try {
            registry.counter(name).increment();
        } catch (RuntimeException metricsFailure) {
            log.debug("Elasticsearch metric recording failed", metricsFailure);
        }
    }
}
