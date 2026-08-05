package com.ibb.yurtlar.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "term_document_requirements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_term_document_requirement",
                        columnNames = {
                                "dormitory_term_id",
                                "document_type_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class TermDocumentRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "dormitory_term_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_requirement_dormitory_term"
            )
    )
    private DormitoryTerm dormitoryTerm;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "document_type_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_requirement_document_type"
            )
    )
    private DocumentType documentType;

    @Column(nullable = false)
    private boolean required;

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