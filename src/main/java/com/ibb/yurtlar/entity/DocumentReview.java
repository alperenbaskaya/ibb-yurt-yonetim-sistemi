package com.ibb.yurtlar.entity;

import com.ibb.yurtlar.enums.DocumentReviewDecision;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_reviews")
@Getter
@Setter
@NoArgsConstructor
public class DocumentReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "student_document_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_document_reviews_student_document"
            )
    )
    private StudentDocument studentDocument;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "reviewer_user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_document_reviews_reviewer"
            )
    )
    private AppUser reviewer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DocumentReviewDecision decision;

    @Column(length = 1000)
    private String comment;

    @Column(
            name = "reviewed_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime reviewedAt;

    @PrePersist
    public void prePersist() {
        reviewedAt = LocalDateTime.now();
    }
}