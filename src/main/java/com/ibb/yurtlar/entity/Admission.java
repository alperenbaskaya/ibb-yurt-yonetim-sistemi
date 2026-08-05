package com.ibb.yurtlar.entity;

import com.ibb.yurtlar.enums.AdmissionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "admissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_admissions_student_term",
                        columnNames = {
                                "student_id",
                                "dormitory_term_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Admission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "student_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_admissions_student"
            )
    )
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "dormitory_term_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_admissions_dormitory_term"
            )
    )
    private DormitoryTerm dormitoryTerm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "dormitory_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_admissions_dormitory"
            )
    )
    private Dormitory dormitory;

    @Column(
            name = "admission_date",
            nullable = false
    )
    private LocalDate admissionDate;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private AdmissionStatus status;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}