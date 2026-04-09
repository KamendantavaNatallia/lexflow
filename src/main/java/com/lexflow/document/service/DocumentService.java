package com.lexflow.document.service;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.repository.LegalCaseRepository;
import com.lexflow.document.model.Document;
import com.lexflow.document.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final LegalCaseRepository legalCaseRepository;

    public DocumentService(DocumentRepository documentRepository,
                           LegalCaseRepository legalCaseRepository) {
        this.documentRepository = documentRepository;
        this.legalCaseRepository = legalCaseRepository;
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public List<Document> getDocumentsByCaseId(Long caseId) {
        return documentRepository.findByLegalCaseId(caseId);
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id).orElse(null);
    }

    public Optional<Document> findDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    public Document saveDocument(Document document) {
        return documentRepository.save(document);
    }

    public Document addDocumentToCase(Long caseId, Document document) {
        LegalCase legalCase = legalCaseRepository.findById(caseId).orElse(null);
        if (legalCase == null) {
            return null;
        }

        document.setLegalCase(legalCase);
        return documentRepository.save(document);
    }

    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id).orElse(null);
        if (document != null) {
            documentRepository.delete(document);
        }
    }
}