package com.lexflow.document.service;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.repository.LegalCaseRepository;
import com.lexflow.document.model.Document;
import com.lexflow.document.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentServiceTest {

    private DocumentRepository documentRepository;
    private LegalCaseRepository legalCaseRepository;
    private DocumentService documentService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        documentRepository = mock(DocumentRepository.class);
        legalCaseRepository = mock(LegalCaseRepository.class);
        documentService = new DocumentService(documentRepository, legalCaseRepository, tempDir.toString());
    }

    @Test
    void getDocumentsByCaseId_shouldReturnDocumentsForCase() {
        Document document = new Document();
        document.setOriginalFileName("contract.pdf");

        when(documentRepository.findByLegalCaseId(1L)).thenReturn(List.of(document));

        List<Document> result = documentService.getDocumentsByCaseId(1L);

        assertEquals(1, result.size());
        assertEquals("contract.pdf", result.get(0).getOriginalFileName());
        verify(documentRepository).findByLegalCaseId(1L);
    }

    @Test
    void getDocumentById_shouldReturnDocument_whenFound() {
        Document document = new Document();
        document.setOriginalFileName("contract.pdf");

        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        Optional<Document> result = documentService.getDocumentById(1L);

        assertTrue(result.isPresent());
        assertEquals("contract.pdf", result.get().getOriginalFileName());
        verify(documentRepository).findById(1L);
    }

    @Test
    void getDocumentById_shouldReturnEmpty_whenNotFound() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Document> result = documentService.getDocumentById(999L);

        assertTrue(result.isEmpty());
        verify(documentRepository).findById(999L);
    }

    @Test
    void saveDocument_shouldThrowException_whenCaseNotFound() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contract.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        when(legalCaseRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> documentService.saveDocument(1L, file)
        );

        assertEquals("Case not found with id: 1", exception.getMessage());
        verify(legalCaseRepository).findById(1L);
        verify(documentRepository, never()).save(any());
    }

    @Test
    void saveDocument_shouldThrowException_whenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> documentService.saveDocument(1L, emptyFile)
        );

        assertEquals("File is empty.", exception.getMessage());
        verify(legalCaseRepository, never()).findById(anyLong());
        verify(documentRepository, never()).save(any());
    }

    @Test
    void saveDocument_shouldThrowException_whenFileTypeIsNotAllowed() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malware.exe",
                "application/octet-stream",
                "binary".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> documentService.saveDocument(1L, file)
        );

        assertEquals("Unsupported file type.", exception.getMessage());
        verify(legalCaseRepository, never()).findById(anyLong());
        verify(documentRepository, never()).save(any());
    }

    @Test
    void saveDocument_shouldSaveDocumentAndFile_whenInputIsValid() throws IOException {
        LegalCase legalCase = new LegalCase();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contract review.pdf",
                "application/pdf",
                "sample pdf content".getBytes()
        );

        when(legalCaseRepository.findById(1L)).thenReturn(Optional.of(legalCase));

        documentService.saveDocument(1L, file);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(captor.capture());

        Document savedDocument = captor.getValue();

        assertEquals("contract review.pdf", savedDocument.getOriginalFileName());
        assertEquals("application/pdf", savedDocument.getContentType());
        assertEquals(file.getSize(), savedDocument.getFileSize());
        assertNotNull(savedDocument.getUploadedAt());
        assertEquals(legalCase, savedDocument.getLegalCase());

        assertNotNull(savedDocument.getStoredFileName());
        assertTrue(savedDocument.getStoredFileName().contains("contract_review.pdf"));

        Path savedPath = tempDir.resolve(savedDocument.getStoredFileName());
        assertTrue(Files.exists(savedPath));
        assertEquals("sample pdf content", Files.readString(savedPath));
    }

    @Test
    void saveDocument_shouldUseFallbackName_whenOriginalFilenameIsMissing() throws IOException {
        LegalCase legalCase = new LegalCase();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                null,
                "application/pdf",
                "content".getBytes()
        );

        when(legalCaseRepository.findById(1L)).thenReturn(Optional.of(legalCase));

        documentService.saveDocument(1L, file);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(captor.capture());

        Document savedDocument = captor.getValue();

        assertTrue(savedDocument.getOriginalFileName() == null || savedDocument.getOriginalFileName().isBlank());
        assertNotNull(savedDocument.getStoredFileName());
        assertTrue(savedDocument.getStoredFileName().contains("_"));

        Path savedPath = tempDir.resolve(savedDocument.getStoredFileName());
        assertTrue(Files.exists(savedPath));
        assertEquals("content", Files.readString(savedPath));
    }

    @Test
    void getFilePath_shouldResolveStoredFileNameAgainstUploadPath() {
        Path result = documentService.getFilePath("abc_contract.pdf");

        assertEquals(tempDir.resolve("abc_contract.pdf").normalize(), result);
    }

    @Test
    void deleteDocument_shouldDoNothing_whenDocumentDoesNotExist() throws IOException {
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        documentService.deleteDocument(1L);

        verify(documentRepository).findById(1L);
        verify(documentRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteDocument_shouldDeleteFileAndRepositoryRecord_whenDocumentExists() throws IOException {
        Document document = new Document();
        document.setStoredFileName("to-delete.pdf");

        Path filePath = tempDir.resolve("to-delete.pdf");
        Files.writeString(filePath, "delete me");

        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        documentService.deleteDocument(1L);

        assertFalse(Files.exists(filePath));
        verify(documentRepository).findById(1L);
        verify(documentRepository).deleteById(1L);
    }

    @Test
    void deleteDocument_shouldDeleteRepositoryRecordEvenIfFileIsMissing() throws IOException {
        Document document = new Document();
        document.setStoredFileName("missing.pdf");

        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        documentService.deleteDocument(1L);

        verify(documentRepository).findById(1L);
        verify(documentRepository).deleteById(1L);
    }
}