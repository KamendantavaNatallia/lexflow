package org.example.service;

import org.example.model.Document;
import org.example.model.LegalCase;
import org.example.repository.DocumentRepository;
import org.example.repository.LegalCaseRepository;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final LegalCaseRepository legalCaseRepository;

    public DocumentService(DocumentRepository documentRepository,
                           LegalCaseRepository legalCaseRepository) {
        this.documentRepository = documentRepository;
        this.legalCaseRepository = legalCaseRepository;
    }

    public Document addDocumentToCase(Long caseId, Document document) {
        LegalCase legalCase = legalCaseRepository.findById(caseId).orElse(null);
        if (legalCase == null) {
            return null;
        }

        document.setLegalCase(legalCase);
        legalCase.getDocuments().add(document);
        legalCaseRepository.save(legalCase);

        return document;
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id).orElse(null);
    }

    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id).orElse(null);
        if (document != null) {
            documentRepository.delete(document);
        }
    }
}