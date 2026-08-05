package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.Dormitory;
import org.springframework.data.jpa.repository.JpaRepository;

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
}