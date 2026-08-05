package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentTypeRepository
        extends JpaRepository<DocumentType, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<DocumentType> findAllByOrderByNameAsc();

    List<DocumentType> findAllByActiveTrueOrderByNameAsc();
}