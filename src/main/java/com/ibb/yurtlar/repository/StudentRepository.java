package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.dto.ReviewerStudentResponse;
import com.ibb.yurtlar.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByIdentityNumber(
            String identityNumber
    );

    boolean existsByUserId(
            Long userId
    );

    @Query("""
            SELECT s
            FROM Student s
            JOIN s.user u
            WHERE LOWER(TRIM(u.email))
                  = LOWER(TRIM(:email))
            """)
    Optional<Student> findByUserEmail(
            @Param("email")
            String email
    );

    @Query("""
            SELECT DISTINCT s
            FROM Student s
            JOIN FETCH s.user studentUser
            JOIN Admission admission
                 ON admission.student = s
            JOIN admission.dormitory dormitory
            JOIN admission.dormitoryTerm term
            WHERE s.id = :studentId
              AND dormitory.id = :dormitoryId
              AND term.active = true
            """)
    Optional<Student>
    findActiveTermStudentByIdAndDormitory(
            @Param("studentId")
            Long studentId,

            @Param("dormitoryId")
            Long dormitoryId
    );

    @Query("""
            SELECT new com.ibb.yurtlar.dto.ReviewerStudentResponse(
                student.id,
                studentUser.firstName,
                studentUser.lastName,
                studentUser.email,
                student.identityNumber,
                student.faculty,
                student.department,
                student.phone
            )
            FROM Admission admission
            JOIN admission.student student
            JOIN student.user studentUser
            WHERE admission.dormitory.id = :dormitoryId
              AND admission.dormitoryTerm.id = :termId
              AND admission.status =
                  com.ibb.yurtlar.enums.AdmissionStatus.APPROVED
            ORDER BY studentUser.firstName ASC,
                     studentUser.lastName ASC
            """)
    List<ReviewerStudentResponse>
    findReviewerStudentsByDormitoryAndTerm(
            @Param("dormitoryId")
            Long dormitoryId,

            @Param("termId")
            Long termId
    );

    @Query("""
            SELECT student
            FROM Admission admission
            JOIN admission.student student
            JOIN FETCH student.user studentUser
            WHERE admission.dormitory.id = :dormitoryId
              AND admission.dormitoryTerm.id = :termId
              AND admission.status =
                  com.ibb.yurtlar.enums.AdmissionStatus.APPROVED
            ORDER BY studentUser.firstName ASC,
                     studentUser.lastName ASC
            """)
    List<Student> findApprovedStudentsByDormitoryAndTerm(
            @Param("dormitoryId") Long dormitoryId,
            @Param("termId") Long termId
    );
}
