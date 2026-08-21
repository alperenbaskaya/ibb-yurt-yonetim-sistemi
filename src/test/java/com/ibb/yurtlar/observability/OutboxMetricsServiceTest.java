package com.ibb.yurtlar.observability;

import com.ibb.yurtlar.enums.OutboxEventStatus;
import com.ibb.yurtlar.repository.OutboxEventRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OutboxMetricsServiceTest {
    @Test
    void emptyAndFailedGaugeReadsSafelyReturnZero() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        OutboxEventRepository repository = mock(OutboxEventRepository.class);
        when(repository.countByStatus(OutboxEventStatus.PENDING)).thenReturn(0L);
        when(repository.findOldestCreatedAtByStatus(OutboxEventStatus.PENDING)).thenReturn(null);
        new OutboxMetricsService(registry, repository);

        assertThat(registry.get("yurtlar_outbox_pending_count").gauge().value()).isZero();
        assertThat(registry.get("yurtlar_outbox_oldest_pending_age_seconds").gauge().value()).isZero();

        when(repository.countByStatus(OutboxEventStatus.PENDING)).thenThrow(new RuntimeException("db down"));
        when(repository.findOldestCreatedAtByStatus(OutboxEventStatus.PENDING))
                .thenThrow(new RuntimeException("db down"));
        assertThat(registry.get("yurtlar_outbox_pending_count").gauge().value()).isZero();
        assertThat(registry.get("yurtlar_outbox_oldest_pending_age_seconds").gauge().value()).isZero();
    }
}
