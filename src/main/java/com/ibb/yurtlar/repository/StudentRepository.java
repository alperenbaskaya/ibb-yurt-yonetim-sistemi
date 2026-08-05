package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long>
{

    boolean existsByIdentityNumber(String identityNumber);

    boolean existsByUserId(Long userId);

}