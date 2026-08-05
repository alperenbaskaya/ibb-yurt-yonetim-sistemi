package com.ibb.yurtlar.entity;

import com.ibb.yurtlar.enums.StudentDocumentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "student_documents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_documents_admission_type",
                        columnNames = {
                                "admission_id",
                                "document_type_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class StudentDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "admission_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_student_documents_admission"
            )
    )
    private Admission admission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "document_type_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_student_documents_document_type"
            )
    )
    private DocumentType documentType;

    @Column(
            name = "original_file_name",
            nullable = false,
            length = 255
    )
    private String originalFileName;

    @Column(
            name = "stored_file_name",
            nullable = false,
            unique = true,
            length = 255
    )
    private String storedFileName;

    @Column(
            name = "file_path",
            nullable = false,
            unique = true,
            length = 500
    )
    private String filePath;

    @Column(
            name = "content_type",
            nullable = false,
            length = 100
    )
    private String contentType;

    @Column(
            name = "file_size",
            nullable = false
    )
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StudentDocumentStatus status;

    @Column(
            name = "uploaded_at",
            nullable = false
    )
    private LocalDateTime uploadedAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        uploadedAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}