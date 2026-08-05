package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.TermDocumentRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional; // ??????????

public interface TermDocumentRequirementRepository
        extends JpaRepository<TermDocumentRequirement, Long> {

    boolean existsByDormitoryTerm_Id(Long dormitoryTermId); //sonradan

    boolean existsByDormitoryTerm_IdAndDocumentType_Id(
            Long dormitoryTermId,
            Long documentTypeId
    );

    List<TermDocumentRequirement>
    findAllByDormitoryTerm_IdOrderByDocumentType_NameAsc(
            Long dormitoryTermId
    );

    List<TermDocumentRequirement>
    findAllByDormitoryTerm_IdAndDocumentType_ActiveTrueOrderByDocumentType_NameAsc(
            Long dormitoryTermId
    );

    Optional<TermDocumentRequirement>
    findByDormitoryTerm_IdAndDocumentType_Id(
            Long dormitoryTermId,
            Long documentTypeId
    );
}