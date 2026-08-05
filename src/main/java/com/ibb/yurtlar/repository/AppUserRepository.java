package com.ibb.yurtlar.repository;

import com.ibb.yurtlar.enums.Role;
import com.ibb.yurtlar.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    boolean existsByEmailIgnoreCase(String email); //büyük küçük harf önemsiz aynı mail var mı?

    boolean existsByEmailIgnoreCaseAndIdNot(
            String email,
            Long id
    );

    long countByRole(Role role);
}

