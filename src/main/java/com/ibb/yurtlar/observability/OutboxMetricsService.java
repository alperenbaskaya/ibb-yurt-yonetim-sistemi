package com.ibb.yurtlar.observability;

import com.ibb.yurtlar.enums.OutboxEventStatus;
import com.ibb.yurtlar.repository.OutboxEventRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class OutboxMetricsService {
    private static final Logger log = LoggerFactory.getLogger(OutboxMetricsService.class);

    private final MeterRegistry registry;
    private final OutboxEventRepository repository;

    public OutboxMetricsService(MeterRegistry registry, OutboxEventRepository repository) {
        this.registry = registry;
        this.repository = repository;
        Gauge.builder("yurtlar_outbox_pending_count", this, service -> service.pendingCount())
                .register(registry);
        Gauge.builder("yurtlar_outbox_oldest_pending_age_seconds", this,
                        service -> service.oldestPendingAgeSeconds())
                .register(registry);
    }

    public void publishSuccess() { increment("yurtlar_outbox_publish_success_total"); }
    public void publishFailure() { increment("yurtlar_outbox_publish_failure_total"); }

    double pendingCount() {
        try {
            return repository.countByStatus(OutboxEventStatus.PENDING);
        } catch (RuntimeException metricsFailure) {
            log.debug("Outbox pending gauge read failed", metricsFailure);
            return 0;
        }
    }

    double oldestPendingAgeSeconds() {
        try {
            LocalDateTime oldest = repository.findOldestCreatedAtByStatus(OutboxEventStatus.PENDING);
            return oldest == null ? 0 : Math.max(0, Duration.between(oldest, LocalDateTime.now()).toSeconds());
        } catch (RuntimeException metricsFailure) {
            log.debug("Outbox age gauge read failed", metricsFailure);
            return 0;
        }
    }

    private void increment(String name) {
        try {
            registry.counter(name).increment();
        } catch (RuntimeException metricsFailure) {
            log.debug("Outbox metric recording failed", metricsFailure);
        }
    }
}
