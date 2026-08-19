# Yurtlar Loki queries

Grafana supplies the selected dashboard time range to every query. Loki labels are deliberately limited to `service`, `environment`, `level`, and `component`; other JSON properties remain query-time fields and structured metadata.

| Use case | LogQL |
|---|---|
| All Yurtlar backend logs | `{service="yurtlar"}` |
| ERROR logs | `{service="yurtlar", level="ERROR"}` |
| WARN and ERROR logs | `{service="yurtlar", level=~"WARN\|ERROR"}` |
| Internal server errors | `{service="yurtlar"} | json | errorCode="INTERNAL_SERVER_ERROR"` |
| Elasticsearch errors | `{service="yurtlar", component="ELASTICSEARCH", level="ERROR"}` |
| Kafka errors | `{service="yurtlar", component="KAFKA", level="ERROR"}` |
| Outbox errors | `{service="yurtlar", component="OUTBOX", level="ERROR"}` |
| DLT activity | `{service="yurtlar", component="KAFKA"} |= "published to DLT"` |
| Authorization failures | `{service="yurtlar", component="SECURITY"} | json | errorCategory="AUTHORIZATION"` |
| Selected environment/component | `{service="yurtlar", environment="$environment", component="$component"}` |

Useful query-time fields include `errorCode`, `errorCategory`, `errorSeverity`, `errorSource`, `eventType`, `consumer`, `operation`, `index`, and `exception`. They are not indexed Loki labels.
