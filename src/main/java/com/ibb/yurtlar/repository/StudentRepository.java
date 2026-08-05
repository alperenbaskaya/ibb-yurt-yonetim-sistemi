package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long>
{

    boolean existsByIdentityNumber(String identityNumber);

    boolean existsByUserId(Long userId);


    // Optional<Student> findByUser_EmailIgnoreCase(String email);
    // JPQL ile yazdık. İlişkili durumları artık JPQL ile ifade ediyoruz.

    @Query("""
        SELECT s
        FROM Student s
        JOIN s.user u
        WHERE LOWER(TRIM(u.email))
              = LOWER(TRIM(:email))
        """)
    Optional<Student> findByUserEmail(
            @Param("email") String email
    );

}

