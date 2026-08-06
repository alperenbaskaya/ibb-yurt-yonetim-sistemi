package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.Dormitory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DormitoryRepository
        extends JpaRepository<Dormitory, Long> {

    boolean existsByNameIgnoreCase(
            String name
    );

    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            Long id
    );

    List<Dormitory> findAllByOrderByNameAsc();

    List<Dormitory> findAllByActiveTrueOrderByNameAsc();

    @Query("""
        SELECT dormitory
        FROM Dormitory dormitory
        ORDER BY dormitory.name ASC
        """)
    List<Dormitory> findAllOrderedByName();
}