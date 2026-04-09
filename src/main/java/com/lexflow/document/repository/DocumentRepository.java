package com.lexflow.document.repository;

import com.lexflow.document.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByLegalCaseId(Long legalCaseId);
}
