package com.ibb.yurtlar.entity;

import com.ibb.yurtlar.enums.AdminScope;
import com.ibb.yurtlar.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_email",
                        columnNames = "email"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "first_name",
            nullable = false,
            length = 100
    )
    private String firstName;

    @Column(
            name = "last_name",
            nullable = false,
            length = 100
    )
    private String lastName;

    @Column(
            nullable = false,
            length = 150
    )
    private String email;

    @Column(
            name = "password_hash",
            nullable = false,
            length = 100
    )
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private Role role;

    @Column(nullable = false)
    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "admin_scope",
            length = 20
    )
    private AdminScope adminScope;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "dormitory_id",
            foreignKey = @ForeignKey(
                    name = "fk_users_dormitory"
            )
    )
    private Dormitory dormitory;
}