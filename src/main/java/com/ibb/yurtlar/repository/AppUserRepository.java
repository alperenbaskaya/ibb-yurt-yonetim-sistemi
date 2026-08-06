package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}

