package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.ibb.yurtlar.enums.AdminScope;


public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByEmailIgnoreCase(String email); //büyük küçük harf önemsiz aynı mail var mı?

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            Long id
    );

    long countByRole(Role role);

    @Query("""
        SELECT u
        FROM AppUser u
        WHERE LOWER(TRIM(u.email))
              = LOWER(TRIM(:email))
        """)
    Optional<AppUser> findByNormalizedEmail(
            @Param("email") String email
    );

    @Query("""
        SELECT u
        FROM AppUser u
        LEFT JOIN FETCH u.dormitory d
        WHERE u.role = :role
          AND d.id = :dormitoryId
        ORDER BY u.firstName ASC,
                 u.lastName ASC
        """)
    List<AppUser> findByRoleAndDormitory(
            @Param("role")
            Role role,

            @Param("dormitoryId")
            Long dormitoryId
    );

    @Query("""
        SELECT COUNT(user)
        FROM AppUser user
        WHERE user.role = :role
          AND user.dormitory.id = :dormitoryId
          AND (:active IS NULL OR user.active = :active)
        """)
    long countByRoleAndDormitoryAndOptionalActive(
            @Param("role")
            Role role,

            @Param("dormitoryId")
            Long dormitoryId,

            @Param("active")
            Boolean active
    );

    @Query("""
        SELECT admin
        FROM AppUser admin
        JOIN FETCH admin.dormitory dormitory
        WHERE admin.role =
              com.ibb.yurtlar.enums.Role.ADMIN
          AND admin.adminScope =
              com.ibb.yurtlar.enums.AdminScope.DORMITORY
        ORDER BY dormitory.name ASC,
                 admin.firstName ASC,
                 admin.lastName ASC
        """)
    List<AppUser> findAllDormitoryManagers();


    @Query("""
        SELECT reviewer
        FROM AppUser reviewer
        JOIN FETCH reviewer.dormitory dormitory
        WHERE reviewer.role =
              com.ibb.yurtlar.enums.Role.REVIEWER
        ORDER BY dormitory.name ASC,
                 reviewer.firstName ASC,
                 reviewer.lastName ASC
        """)
    List<AppUser> findAllDormitoryReviewers();

    @Query("""
        SELECT COUNT(admin)
        FROM AppUser admin
        WHERE admin.role =
              com.ibb.yurtlar.enums.Role.ADMIN
          AND admin.adminScope = :adminScope
        """)
    long countAdminsByScope(
            @Param("adminScope")
            AdminScope adminScope
    );

    @Query("""
        SELECT COUNT(reviewer)
        FROM AppUser reviewer
        WHERE reviewer.role =
              com.ibb.yurtlar.enums.Role.REVIEWER
          AND (:active IS NULL
               OR reviewer.active = :active)
        """)
    long countReviewersByOptionalActive(
            @Param("active")
            Boolean active
    );
}

