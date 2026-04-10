package com.lexflow.document.service;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.service.LegalCaseService;
import com.lexflow.document.model.Document;
import com.lexflow.document.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "image/png",
            "image/jpeg"
    );

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
            throw new IllegalArgumentException("Please select a file to upload.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "File type is not allowed. Please upload PDF, DOC, DOCX, TXT, PNG, or JPG."
            );
        }

        Files.createDirectories(uploadPath);

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            originalFileName = "uploaded-file";
        }

        String safeOriginalFileName = originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String storedFileName = UUID.randomUUID() + "_" + safeOriginalFileName;
        Path targetLocation = uploadPath.resolve(storedFileName).normalize();

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

    public String formatFileSize(Long sizeInBytes) {
        if (sizeInBytes == null) {
            return "Unknown size";
        }

        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        }

        double sizeInKb = sizeInBytes / 1024.0;
        if (sizeInKb < 1024) {
            return String.format("%.1f KB", sizeInKb);
        }

        double sizeInMb = sizeInKb / 1024.0;
        return String.format("%.1f MB", sizeInMb);
    }

    public String getFriendlyFileType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "Unknown";
        }

        return switch (contentType) {
            case "application/pdf" -> "PDF";
            case "application/msword" -> "DOC";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "DOCX";
            case "text/plain" -> "TXT";
            case "image/png" -> "PNG";
            case "image/jpeg" -> "JPG";
            default -> contentType;
        };
    }
}