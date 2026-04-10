package com.lexflow.document.service;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.service.LegalCaseService;
import com.lexflow.document.model.Document;
import com.lexflow.document.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final LegalCaseService legalCaseService;
    private final Path uploadPath;

    public DocumentService(DocumentRepository documentRepository,
                           LegalCaseService legalCaseService,
                           @Value("${app.upload-dir}") String uploadDir) {
        this.documentRepository = documentRepository;
        this.legalCaseService = legalCaseService;
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id).orElse(null);
    }

    public void addDocumentToCase(Long caseId, MultipartFile file) throws IOException {
        LegalCase legalCase = legalCaseService.getCaseById(caseId);
        if (legalCase == null) {
            throw new IllegalArgumentException("Case not found");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        Files.createDirectories(uploadPath);

        String originalFileName = file.getOriginalFilename();
        String safeOriginalFileName = originalFileName != null ? originalFileName : "uploaded-file";
        String storedFileName = UUID.randomUUID() + "_" + safeOriginalFileName;

        Path targetLocation = uploadPath.resolve(storedFileName);
        Files.copy(file.getInputStream(), targetLocation);

        Document document = new Document();
        document.setOriginalFileName(safeOriginalFileName);
        document.setStoredFileName(storedFileName);
        document.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        document.setFileSize(file.getSize());
        document.setLegalCase(legalCase);

        documentRepository.save(document);
    }

    public Path getDocumentPath(Document document) {
        return uploadPath.resolve(document.getStoredFileName()).normalize();
    }

    public void deleteDocument(Long id) {
        Optional<Document> optionalDocument = documentRepository.findById(id);
        if (optionalDocument.isEmpty()) {
            return;
        }

        Document document = optionalDocument.get();

        try {
            Files.deleteIfExists(getDocumentPath(document));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file from disk", e);
        }

        documentRepository.deleteById(id);
    }
}