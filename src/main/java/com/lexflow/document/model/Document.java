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

    @NotBlank(message = "Document name is required")
    @Column(nullable = false)
    private String fileName;

    @NotBlank(message = "Document type is required")
    @Column(nullable = false)
    private String fileType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private LegalCase legalCase;
}