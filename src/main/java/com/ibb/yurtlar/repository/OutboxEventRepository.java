package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.OutboxEvent;
import com.ibb.yurtlar.enums.OutboxEventStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, Long> {

    @Query("""
            SELECT event
            FROM OutboxEvent event
            WHERE event.status = :status
            ORDER BY event.createdAt ASC, event.id ASC
            """)
    List<OutboxEvent> findByStatusOldestFirst(
            @Param("status") OutboxEventStatus status,
            Pageable pageable
    );
}