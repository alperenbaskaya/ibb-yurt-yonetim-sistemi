# Yurtlar metrics queries

These PromQL expressions are intended for the local Grafana Prometheus data source. The short ranges make changes visible during demonstrations.

| Panel | PromQL |
|---|---|
| Request rate | `sum(rate(http_server_requests_seconds_count{job="yurtlar-backend"}[1m]))` |
| 4xx rate | `sum(rate(http_server_requests_seconds_count{job="yurtlar-backend",status=~"4.."}[1m]))` |
| 5xx rate | `sum(rate(http_server_requests_seconds_count{job="yurtlar-backend",status=~"5.."}[1m]))` |
| HTTP error percentage | `100 * sum(rate(http_server_requests_seconds_count{job="yurtlar-backend",status=~"4..|5.."}[1m])) / clamp_min(sum(rate(http_server_requests_seconds_count{job="yurtlar-backend"}[1m])), 0.001)` |
| p95 latency | `histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{job="yurtlar-backend"}[5m])))` |
| p99 latency | `histogram_quantile(0.99, sum by (le) (rate(http_server_requests_seconds_bucket{job="yurtlar-backend"}[5m])))` |
| Top failing URI templates | `topk(10, sum by (uri) (rate(http_server_requests_seconds_count{job="yurtlar-backend",status=~"4..|5.."}[5m])))` |
| Errors by code | `sum by (code) (increase(yurtlar_errors_total[5m]))` |
| Errors by category | `sum by (category) (increase(yurtlar_errors_total[5m]))` |
| Errors by severity | `sum by (severity) (increase(yurtlar_errors_total[5m]))` |
| Errors by source | `sum by (source) (increase(yurtlar_errors_total[5m]))` |
| Kafka DLT increase | `sum by (eventType, consumer) (increase(yurtlar_kafka_dlt_total[5m]))` |
| Kafka processing errors | `sum by (eventType, consumer) (increase(yurtlar_kafka_processing_errors_total[5m]))` |
| Kafka duplicates | `sum by (eventType, consumer) (increase(yurtlar_kafka_duplicates_total[5m]))` |
| Outbox pending | `yurtlar_outbox_pending_count` |
| Outbox oldest pending age | `yurtlar_outbox_oldest_pending_age_seconds` |
| Outbox publish failures | `increase(yurtlar_outbox_publish_failure_total[5m])` |
| Outbox publish success rate | `sum(rate(yurtlar_outbox_publish_success_total[5m]))` |
| Outbox publish failure rate | `sum(rate(yurtlar_outbox_publish_failure_total[5m]))` |
| Elasticsearch indexing failures | `increase(yurtlar_elasticsearch_index_failure_total[5m])` |
| Elasticsearch search failures | `increase(yurtlar_elasticsearch_search_failure_total[5m])` |
| Elasticsearch reindex failures | `increase(yurtlar_elasticsearch_reindex_failure_total[5m])` |
| Elasticsearch indexing success | `increase(yurtlar_elasticsearch_index_success_total[5m])` |
| Elasticsearch search success | `increase(yurtlar_elasticsearch_search_success_total[5m])` |
| Elasticsearch reindex success | `increase(yurtlar_elasticsearch_reindex_success_total[5m])` |
| Kafka consumer lag | `sum by (consumergroup, topic) (kafka_consumergroup_lag)` |
| Backend target health | `up{job="yurtlar-backend"}` |
| Kafka exporter health | `up{job="kafka-exporter"}` |
| Availability (15m) | `100 * (1 - sum(increase(http_server_requests_seconds_count{job="yurtlar-backend",status=~"5.."}[15m])) / clamp_min(sum(increase(http_server_requests_seconds_count{job="yurtlar-backend"}[15m])), 1))` |
| Error budget remaining (15m) | `clamp_min(100 * (1 - ((sum(increase(http_server_requests_seconds_count{job="yurtlar-backend",status=~"5.."}[15m])) / clamp_min(sum(increase(http_server_requests_seconds_count{job="yurtlar-backend"}[15m])), 1)) / 0.005)), 0)` |

The `uri` label in the standard HTTP metric is the framework route template, not the raw request URL, preventing IDs embedded in paths from becoming labels.

## Alert expressions

| Alert | PromQL |
|---|---|
| HIGH_5XX_RATE | `(sum(rate(http_server_requests_seconds_count{job="yurtlar-backend",status=~"5.."}[5m])) / clamp_min(sum(rate(http_server_requests_seconds_count{job="yurtlar-backend"}[5m])), 0.001)) and on() (sum(increase(http_server_requests_seconds_count{job="yurtlar-backend"}[5m])) >= 20)` |
| KAFKA_DLT_ACTIVITY | `sum(increase(yurtlar_kafka_dlt_total[5m]))` |
| OUTBOX_STUCK | `max(yurtlar_outbox_oldest_pending_age_seconds)` |
| OUTBOX_PUBLISH_FAILURES | `sum(increase(yurtlar_outbox_publish_failure_total[5m]))` |
| ELASTICSEARCH_FAILURES | `sum(increase({__name__=~"yurtlar_elasticsearch_(index|search|reindex)_failure_total"}[5m]))` |
| KAFKA_CONSUMER_LAG | `sum(kafka_consumergroup_lag{consumergroup="document-upload-idempotency-group-v1",topic="dormitory-activity-events"})` |
| INTERNAL_CRITICAL_ERRORS | `sum(increase(yurtlar_errors_total{category="INTERNAL",severity="CRITICAL"}[5m]))` |

The availability SLI treats only 5xx responses as service failures. Validation, authorization, expected 404, and expected business-conflict 4xx responses do not consume the 99.5% availability objective. The 15-minute window is intentionally short for demonstrations and is not a production compliance window.
