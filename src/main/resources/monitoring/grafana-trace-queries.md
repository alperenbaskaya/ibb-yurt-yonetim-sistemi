# Grafana trace queries and correlation

Tempo uses TraceQL. Select the provisioned `Tempo` datasource in Grafana Explore.

## Recent backend traces

```traceql
{ resource.service.name = "yurtlar-backend" }
```

Set the Explore time picker to the desired recent window; start with the last 15 minutes for local demos.

## HTTP error traces

```traceql
{ resource.service.name = "yurtlar-backend" && span.http.response.status_code >= 400 }
```

## Slow traces

```traceql
{ resource.service.name = "yurtlar-backend" } && duration > 500ms
```

## Service and span status

```traceql
{ resource.service.name = "yurtlar-backend" && status = error }
```

Use `{ status = error }` to search errors across every service if more services are added later.

## Trace to Loki

Open a trace in Tempo and use the Logs link. The provisioned correlation searches Loki for the same `traceId` in the `yurtlar` service stream, with one minute of context on each side of the span.

## Loki traceId to Tempo

In Loki Explore, keep `traceId` as a parsed JSON field or structured metadata field; it is intentionally not an indexed label:

```logql
{service="yurtlar"} | json | traceId="0123456789abcdef0123456789abcdef"
```

Copy the resulting `traceId` into Tempo's trace-ID search when navigating in the opposite direction.

## Prometheus exemplar to trace

Use a histogram panel based on `http_server_requests_seconds_bucket`, enable exemplars in the panel, and select the `Tempo` destination. Micrometer Tracing supplies the exemplar context automatically and the Prometheus datasource maps the `trace_id` exemplar to Tempo. Only sampled traces are attached; local sampling defaults to 1.0.

## Elasticsearch operations

No extra Elasticsearch observations are introduced in this milestone. Elasticsearch work invoked by HTTP remains within the automatic HTTP trace, while the structured Elasticsearch logs carry the same active `traceId` and `spanId`. Find those logs with:

```logql
{service="yurtlar", component="ELASTICSEARCH"} | json | traceId="0123456789abcdef0123456789abcdef"
```

## Outbox and Kafka boundary

The scheduled outbox publisher and later Kafka consumer are asynchronous and are not forced into the originating HTTP trace. `traceId` is technical identity for one execution trace; the existing `eventId` is business/event identity across asynchronous processing. Kafka correctness never depends on trace propagation. Correlate these stages with existing bounded operational fields and the existing event identity where it is already legitimately logged; do not add event IDs as Loki labels or Prometheus tags.
