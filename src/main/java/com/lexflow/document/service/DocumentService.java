package com.lexflow.document.service;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.repository.LegalCaseRepository;
import com.lexflow.document.model.Document;
import com.lexflow.document.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final LegalCaseRepository legalCaseRepository;
    private final Path uploadPath;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "image/png",
            "image/jpeg"
    );

    public DocumentService(
            DocumentRepository documentRepository,
            LegalCaseRepository legalCaseRepository,
            @Value("${app.upload-dir}") String uploadDir
    ) {
        this.documentRepository = documentRepository;
        this.legalCaseRepository = legalCaseRepository;
        this.uploadPath = Path.of(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public List<Document> getDocumentsByCaseId(Long caseId) {
        return documentRepository.findByLegalCaseId(caseId);
    }

    public void saveDocument(Long caseId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported file type.");
        }

        LegalCase legalCase = legalCaseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found with id: " + caseId));

        String originalFileName = file.getOriginalFilename();
        String safeOriginalFileName = originalFileName == null
                ? "file"
                : originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");

        String storedFileName = UUID.randomUUID() + "_" + safeOriginalFileName;
        Path targetLocation = uploadPath.resolve(storedFileName).normalize();

        if (!targetLocation.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Invalid file path.");
        }

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        Document document = new Document();
        document.setOriginalFileName(originalFileName);
        document.setStoredFileName(storedFileName);
        document.setContentType(contentType);
        document.setFileSize(file.getSize());
        document.setUploadedAt(LocalDateTime.now());
        document.setLegalCase(legalCase);

        documentRepository.save(document);
    }

    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    public Path getFilePath(String storedFileName) {
        return uploadPath.resolve(storedFileName).normalize();
    }

    public void deleteDocument(Long id) throws IOException {
        Optional<Document> optionalDocument = documentRepository.findById(id);

        if (optionalDocument.isPresent()) {
            Document document = optionalDocument.get();

            Path filePath = getFilePath(document.getStoredFileName());
            Files.deleteIfExists(filePath);

            documentRepository.deleteById(id);
        }
    }
}