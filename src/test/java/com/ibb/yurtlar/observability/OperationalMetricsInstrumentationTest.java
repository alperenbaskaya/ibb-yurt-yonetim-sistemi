package com.ibb.yurtlar.observability;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import com.ibb.yurtlar.exception.DuplicateKafkaEventException;
import com.ibb.yurtlar.kafka.DocumentUploadedEventListener;
import com.ibb.yurtlar.kafka.event.DocumentUploadedEvent;
import com.ibb.yurtlar.service.DocumentUploadedEventConsumerService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import com.ibb.yurtlar.config.CustomAuthenticationEntryPoint;
import com.ibb.yurtlar.config.CustomAccessDeniedHandler;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OperationalMetricsInstrumentationTest {
    @Test
    void kafkaMetricsUseOnlyBoundedTags() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        KafkaMetricsService metrics = new KafkaMetricsService(registry);
        metrics.processingError("DOCUMENT_UPLOADED", "document-upload-idempotency-group-v1");
        metrics.deadLetter("event-123", "user@example.com");
        metrics.duplicate("DOCUMENT_UPLOADED", "document-upload-idempotency-group-v1");

        assertThat(registry.get("yurtlar_kafka_processing_errors_total")
                .tags("eventType", "DOCUMENT_UPLOADED",
                        "consumer", "document-upload-idempotency-group-v1")
                .counter().count()).isEqualTo(1);
        assertThat(registry.get("yurtlar_kafka_dlt_total")
                .tags("eventType", "UNKNOWN", "consumer", "UNKNOWN")
                .counter().count()).isEqualTo(1);
        assertThat(registry.getMeters().toString()).doesNotContain("event-123", "example.com");
    }

    @Test
    void elasticsearchSuccessAndFailureCountersIncrement() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        ElasticsearchMetricsService metrics = new ElasticsearchMetricsService(registry);
        metrics.indexSuccess(); metrics.indexFailure();
        metrics.searchSuccess(); metrics.searchFailure();
        metrics.reindexSuccess(); metrics.reindexFailure();

        assertThat(registry.getMeters()).hasSize(6);
        assertThat(registry.get("yurtlar_elasticsearch_index_success_total").counter().count()).isEqualTo(1);
        assertThat(registry.get("yurtlar_elasticsearch_search_failure_total").counter().count()).isEqualTo(1);
        assertThat(registry.get("yurtlar_elasticsearch_reindex_failure_total").counter().count()).isEqualTo(1);
    }

    @Test
    void duplicateKafkaBehaviorStillCompletesNormallyAndIncrementsMetric() throws Exception {
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        DocumentUploadedEventConsumerService consumer = mock(DocumentUploadedEventConsumerService.class);
        KafkaMetricsService metrics = mock(KafkaMetricsService.class);
        DocumentUploadedEvent event = new DocumentUploadedEvent("private-event-id",
                "DOCUMENT_UPLOADED", 1L, 2L, 3L, 4L, 5L, LocalDateTime.now());
        when(objectMapper.readValue("payload", DocumentUploadedEvent.class)).thenReturn(event);
        doThrow(new DuplicateKafkaEventException("private-event-id", "consumer"))
                .when(consumer).process(event);
        DocumentUploadedEventListener listener =
                new DocumentUploadedEventListener(objectMapper, consumer, metrics);

        listener.listen(new ConsumerRecord<>("dormitory-activity-events", 0, 1, "key", "payload"));

        verify(consumer).process(event);
        verify(metrics).duplicate("DOCUMENT_UPLOADED", "document-upload-idempotency-group-v1");
    }

    @Test
    void preMvcSecurityHandlersRecordExactlyOneSecurityMetric() throws Exception {
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        ErrorMetricsService metrics = mock(ErrorMetricsService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getRequestURI()).thenReturn("/api/private/123");
        when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));

        new CustomAuthenticationEntryPoint(objectMapper, metrics).commence(
                request, response, new BadCredentialsException("token detail"));
        new CustomAccessDeniedHandler(objectMapper, metrics).handle(
                request, response, new AccessDeniedException("role detail"));

        verify(metrics).record(eq("AUTHENTICATION_REQUIRED"),
                eq(org.springframework.http.HttpStatus.UNAUTHORIZED), eq(ErrorSource.SECURITY), any());
        verify(metrics).record(eq("ACCESS_DENIED"),
                eq(org.springframework.http.HttpStatus.FORBIDDEN), eq(ErrorSource.SECURITY), any());
    }
}
