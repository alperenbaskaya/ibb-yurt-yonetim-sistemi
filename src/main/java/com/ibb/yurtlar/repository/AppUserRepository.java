package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
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
}

