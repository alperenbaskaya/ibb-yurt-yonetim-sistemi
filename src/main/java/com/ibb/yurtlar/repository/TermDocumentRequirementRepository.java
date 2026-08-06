package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.TermDocumentRequirement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
        SELECT requirement
        FROM TermDocumentRequirement requirement
        JOIN FETCH requirement.documentType documentType
        JOIN FETCH requirement.dormitoryTerm dormitoryTerm
        WHERE dormitoryTerm.id = :dormitoryTermId
          AND requirement.required = true
          AND documentType.active = true
        ORDER BY documentType.name ASC
        """)
    List<TermDocumentRequirement>
    findRequiredDocumentsByDormitoryTerm(
            @Param("dormitoryTermId")
            Long dormitoryTermId
    );
}