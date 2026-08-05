package com.ibb.yurtlar.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "dormitory_terms")
@Getter
@Setter
@NoArgsConstructor
public class DormitoryTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDate documentUploadStartDate;

    @Column(nullable = false)
    private LocalDate documentUploadEndDate;

    @Column(nullable = false)
    private boolean active;
}