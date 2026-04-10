package com.lexflow.document.model;

import com.lexflow.case_.model.LegalCase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Original file name is required")
    @Column(nullable = false)
    private String originalFileName;

    @NotBlank(message = "Stored file name is required")
    @Column(nullable = false, unique = true)
    private String storedFileName;

    @NotBlank(message = "Content type is required")
    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private LegalCase legalCase;
}