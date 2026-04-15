package com.lexflow.document.service;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.service.LegalCaseService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentServiceTest {

    private DocumentRepository documentRepository;
    private LegalCaseService legalCaseService;
    private DocumentService documentService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        documentRepository = mock(DocumentRepository.class);
        legalCaseService = mock(LegalCaseService.class);
        documentService = new DocumentService(documentRepository, legalCaseService, tempDir.toString());
    }

    @Test
    void getDocumentById_shouldReturnDocument_whenFound() {
        Document document = new Document();
        document.setOriginalFileName("contract.pdf");

        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));

        Document result = documentService.getDocumentById(1L);

        assertNotNull(result);
        assertEquals("contract.pdf", result.getOriginalFileName());
        verify(documentRepository).findById(1L);
    }

    @Test
    void getDocumentById_shouldReturnNull_whenNotFound() {
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        Document result = documentService.getDocumentById(999L);

        assertNull(result);
        verify(documentRepository).findById(999L);
    }

    @Test
    void addDocumentToCase_shouldThrowException_whenCaseNotFound() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contract.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        when(legalCaseService.getCaseById(1L)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> documentService.addDocumentToCase(1L, file)
        );

        assertEquals("Case not found", exception.getMessage());
        verify(legalCaseService).getCaseById(1L);
        verify(documentRepository, never()).save(any());
    }

    @Test
    void addDocumentToCase_shouldThrowException_whenFileIsEmpty() {
        LegalCase legalCase = new LegalCase();

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        when(legalCaseService.getCaseById(1L)).thenReturn(legalCase);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> documentService.addDocumentToCase(1L, emptyFile)
        );

        assertEquals("Please select a file to upload.", exception.getMessage());
        verify(legalCaseService).getCaseById(1L);
        verify(documentRepository, never()).save(any());
    }

    @Test
    void addDocumentToCase_shouldThrowException_whenFileTypeIsNotAllowed() {
        LegalCase legalCase = new LegalCase();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malware.exe",
                "application/octet-stream",
                "binary".getBytes()
        );

        when(legalCaseService.getCaseById(1L)).thenReturn(legalCase);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> documentService.addDocumentToCase(1L, file)
        );

        assertEquals("File type is not allowed. Please upload PDF, DOC, DOCX, TXT, PNG, or JPG.", exception.getMessage());
        verify(legalCaseService).getCaseById(1L);
        verify(documentRepository, never()).save(any());
    }

    @Test
    void addDocumentToCase_shouldSaveDocumentAndFile_whenInputIsValid() throws IOException {
        LegalCase legalCase = new LegalCase();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contract review.pdf",
                "application/pdf",
                "sample pdf content".getBytes()
        );

        when(legalCaseService.getCaseById(1L)).thenReturn(legalCase);

        documentService.addDocumentToCase(1L, file);

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
    void addDocumentToCase_shouldUseFallbackName_whenOriginalFilenameIsMissing() throws IOException {
        LegalCase legalCase = new LegalCase();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                null,
                "application/pdf",
                "content".getBytes()
        );

        when(legalCaseService.getCaseById(1L)).thenReturn(legalCase);

        documentService.addDocumentToCase(1L, file);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).save(captor.capture());

        Document savedDocument = captor.getValue();

        assertEquals("uploaded-file", savedDocument.getOriginalFileName());
        assertTrue(savedDocument.getStoredFileName().contains("uploaded-file"));
    }

    @Test
    void getDocumentPath_shouldResolveStoredFileNameAgainstUploadPath() {
        Document document = new Document();
        document.setStoredFileName("abc_contract.pdf");

        Path result = documentService.getDocumentPath(document);

        assertEquals(tempDir.resolve("abc_contract.pdf").normalize(), result);
    }

    @Test
    void deleteDocument_shouldDoNothing_whenDocumentDoesNotExist() {
        when(documentRepository.findById(1L)).thenReturn(Optional.empty());

        documentService.deleteDocument(1L);

        verify(documentRepository).findById(1L);
        verify(documentRepository, never()).deleteById(any());
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
    void formatFileSize_shouldReturnUnknown_whenNull() {
        assertEquals("Unknown size", documentService.formatFileSize(null));
    }

    @Test
    void formatFileSize_shouldReturnBytes_whenLessThanOneKb() {
        assertEquals("512 B", documentService.formatFileSize(512L));
    }

    @Test
    void formatFileSize_shouldReturnKb_whenLessThanOneMb() {
        assertEquals("1.5 KB", documentService.formatFileSize(1536L));
    }

    @Test
    void formatFileSize_shouldReturnMb_whenOneMbOrMore() {
        assertEquals("1.5 MB", documentService.formatFileSize(1572864L));
    }

    @Test
    void getFriendlyFileType_shouldReturnUnknown_whenNull() {
        assertEquals("Unknown", documentService.getFriendlyFileType(null));
    }

    @Test
    void getFriendlyFileType_shouldReturnUnknown_whenBlank() {
        assertEquals("Unknown", documentService.getFriendlyFileType(" "));
    }

    @Test
    void getFriendlyFileType_shouldReturnFriendlyLabels_forKnownTypes() {
        assertEquals("PDF", documentService.getFriendlyFileType("application/pdf"));
        assertEquals("DOC", documentService.getFriendlyFileType("application/msword"));
        assertEquals("DOCX", documentService.getFriendlyFileType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        assertEquals("TXT", documentService.getFriendlyFileType("text/plain"));
        assertEquals("PNG", documentService.getFriendlyFileType("image/png"));
        assertEquals("JPG", documentService.getFriendlyFileType("image/jpeg"));
    }

    @Test
    void getFriendlyFileType_shouldReturnOriginalType_forUnknownTypes() {
        assertEquals("application/zip", documentService.getFriendlyFileType("application/zip"));
    }
}
