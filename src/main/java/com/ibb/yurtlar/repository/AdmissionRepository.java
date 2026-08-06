package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.Admission;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ibb.yurtlar.enums.AdmissionStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import java.util.List;

public interface AdmissionRepository extends JpaRepository<Admission, Long> {

    boolean existsByDormitoryTerm_Id(Long dormitoryTermId);

    boolean existsByStudent_IdAndDormitoryTerm_Id(Long studentId, Long dormitoryTermId);

    List<Admission>
    findAllByStudent_IdOrderByDormitoryTerm_StartDateDesc(Long studentId);

    @Query("""
        SELECT a
        FROM Admission a
        WHERE (:termId IS NULL
               OR a.dormitoryTerm.id = :termId)
          AND (:status IS NULL
               OR a.status = :status)
          AND (:dormitoryName IS NULL
               OR LOWER(a.dormitory.name)
                    LIKE LOWER(CONCAT('%', :dormitoryName, '%')))
        ORDER BY a.dormitoryTerm.startDate DESC,
                 a.createdAt DESC
        """)
    List<Admission> findByFilters(
            @Param("termId") Long termId,
            @Param("status") AdmissionStatus status,
            @Param("dormitoryName") String dormitoryName
    );

    long countByDormitoryTerm_IdAndStatus(
            Long dormitoryTermId,
            AdmissionStatus status
    );

    long countByDormitoryTerm_Id(Long dormitoryTermId);

    Optional<Admission> findByStudent_IdAndDormitoryTerm_ActiveTrue(
            Long studentId
    );

    List<Admission>
    findTop10ByDormitoryTerm_IdOrderByCreatedAtDesc(
            Long dormitoryTermId
    );


    long countByDormitoryTerm_IdAndDormitory_Id(
            Long dormitoryTermId,
            Long dormitoryId
    );

    long countByDormitoryTerm_IdAndDormitory_IdAndStatus(
            Long dormitoryTermId,
            Long dormitoryId,
            AdmissionStatus status
    );

    List<Admission>
    findTop10ByDormitoryTerm_IdAndDormitory_IdOrderByCreatedAtDesc(
            Long dormitoryTermId,
            Long dormitoryId
    );

    @Query("""
        SELECT a
        FROM Admission a
        JOIN FETCH a.student student
        JOIN FETCH student.user user
        JOIN FETCH a.dormitoryTerm term
        JOIN FETCH a.dormitory dormitory
        WHERE LOWER(TRIM(user.email))
              = LOWER(TRIM(:email))
          AND term.active = true
        """)
    Optional<Admission> findActiveAdmissionByStudentEmail(
            @Param("email") String email
    );

}