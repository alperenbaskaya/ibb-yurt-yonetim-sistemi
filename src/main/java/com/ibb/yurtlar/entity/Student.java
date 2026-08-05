package com.ibb.yurtlar.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "students",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_students_identity_number",
                        columnNames = "identity_number"
                ),
                @UniqueConstraint(
                        name = "uk_students_user_id",
                        columnNames = "user_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "identity_number",
            nullable = false,
            length = 11
    )
    private String identityNumber;

    @Column(nullable = false, length = 150)
    private String faculty;

    @Column(nullable = false, length = 150)
    private String department;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_students_user"
            )
    )
    private AppUser user;
}