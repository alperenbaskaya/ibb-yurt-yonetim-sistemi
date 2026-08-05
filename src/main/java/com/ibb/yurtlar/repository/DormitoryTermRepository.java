package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.DormitoryTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface DormitoryTermRepository extends JpaRepository<DormitoryTerm, Long> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<DormitoryTerm> findAllByActiveTrue();

    Optional<DormitoryTerm> findByActiveTrue();
}