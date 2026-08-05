package com.ibb.yurtlar.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "dormitories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_dormitories_name",
                        columnNames = "name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Dormitory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            length = 150
    )
    private String name;

    @Column(
            length = 500
    )
    private String address;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private boolean active;

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