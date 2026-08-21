# Yurtlar observability stack

## Signal architecture

```text
Metrics: Spring Boot -> /actuator/prometheus -> Prometheus -> Grafana
Logs:    Spring JSON file -> Grafana Alloy -> Loki -> Grafana
Traces:  Spring Boot OTLP HTTP -> Grafana Alloy -> Tempo -> Grafana
```

MySQL, Kafka, and Elasticsearch business behavior is independent of the monitoring stack. A telemetry outage must not decide whether a business operation succeeds.

## Local URLs

| Component | URL |
|---|---|
| Backend application | `http://localhost:8080` |
| Backend management health | `http://localhost:8081/actuator/health` |
| Backend Prometheus metrics | `http://localhost:8081/actuator/prometheus` |
| Prometheus | `http://localhost:9090` |
| Grafana | `http://localhost:3000` |
| Loki | `http://localhost:3100/ready` |
| Tempo | `http://localhost:3200/ready` |
| Alloy | `http://localhost:12345` |
| Alloy OTLP HTTP | `http://localhost:4318/v1/traces` |
| Kafka UI | `http://localhost:8090` |
| Kafka exporter | `http://localhost:9308/metrics` |
| Elasticsearch | `http://localhost:9200` |

## Start the local stack

From the repository root:

```powershell
docker compose -f src/main/resources/docker-compose.yaml up -d
```

Start the Spring Boot application on the Windows host with its normal database and JWT environment variables. The repository-relative `logs` directory is bind-mounted read-only into Alloy, and Spring exports traces to Alloy's host-published port 4318.

## Provisioning

Grafana reads datasources, dashboards, and alert rules from `monitoring/grafana/provisioning`. Dashboard JSON is mounted at `/var/lib/grafana/dashboards` and loaded into the **Yurtlar Observability** folder. Provisioning is reapplied when Grafana starts; dashboard files remain the source of truth.

Provisioned dashboards:

- **Yurtlar - Error Overview**
- **Yurtlar - Incident Drilldown**

Provisioned alerts are evaluated every 30 seconds. They include bounded `service`, `environment`, `severity`, and `component` labels. Notification contact points are intentionally outside this local/demo milestone.

## Verify each signal

Prometheus:

1. Open `http://localhost:9090/targets`.
2. Confirm `yurtlar-backend` and `kafka-exporter` are UP.
3. Query `up{job="yurtlar-backend"}`.

Logs:

1. Confirm `logs/yurtlar.json` is receiving structured JSON lines.
2. In Grafana Explore select Loki.
3. Query `{service="yurtlar"}`.
4. For traced requests, expand a record and verify `traceId` and `spanId` fields exist as structured metadata, not labels.

Traces:

1. Send an ordinary request to the backend.
2. In Grafana Explore select Tempo.
3. Query `{ resource.service.name = "yurtlar-backend" }` over the last 15 minutes.
4. Open a trace and use **Logs for this span** to navigate to Loki.
5. HTTP histogram panels can expose sampled trace exemplars that link back to Tempo.

## Availability SLO

The overview uses a presentation-oriented 99.5% availability objective over 15 minutes:

```text
availability = (all relevant HTTP responses - 5xx responses) / all relevant HTTP responses
```

Expected validation, authentication, authorization, not-found, and business-conflict 4xx outcomes are client/business outcomes and do not consume this service-availability budget. Every 5xx consumes the 0.5% error budget.

## Planned manual failure demonstrations

These are intentionally deferred until the manual-test milestone:

- Invalid request: produce a validation 400 and confirm metrics/log classification without an availability failure.
- Forbidden request: produce a 403 and confirm SECURITY/AUTHORIZATION telemetry.
- Unexpected failure: exercise a controlled 500 and follow metric -> exemplar/trace -> correlated logs.
- Kafka DLT: exercise the existing recovery path and verify DLT metrics, logs, and alert state without changing retry or acknowledgement semantics.
- Outbox pending: pause delivery safely, observe pending count and oldest age, and verify `OUTBOX_STUCK` after its hold period.
- Elasticsearch outage: stop Elasticsearch, exercise an Elasticsearch-only capability, and verify safe HTTP behavior, failure telemetry, and the Elasticsearch alert.

Do not perform destructive data manipulation merely to trigger a dashboard or alert.
