package com.lexflow.document.controller;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.service.LegalCaseService;
import com.lexflow.document.model.Document;
import com.lexflow.document.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean(name = "legalCaseService")
    private LegalCaseService legalCaseService;

    @Test
    void showDocumentForm_shouldReturnDocumentForm_whenCaseExists() throws Exception {
        LegalCase legalCase = new LegalCase();
        legalCase.setTitle("Contract Review");

        when(legalCaseService.getCaseById(1L)).thenReturn(legalCase);

        mockMvc.perform(get("/cases/1/documents/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("document-form"))
                .andExpect(model().attribute("legalCase", legalCase));

        verify(legalCaseService).getCaseById(1L);
    }

    @Test
    void showDocumentForm_shouldRedirectToCases_whenCaseDoesNotExist() throws Exception {
        when(legalCaseService.getCaseById(999L)).thenReturn(null);

        mockMvc.perform(get("/cases/999/documents/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases"));

        verify(legalCaseService).getCaseById(999L);
    }

    @Test
    void addDocument_shouldRedirectToCases_whenCaseDoesNotExist() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contract.pdf",
                "application/pdf",
                "content".getBytes()
        );

        when(legalCaseService.getCaseById(1L)).thenReturn(null);

        mockMvc.perform(multipart("/cases/1/documents").file(file))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases"));

        verify(legalCaseService).getCaseById(1L);
        verify(documentService, never()).addDocumentToCase(any(), any());
    }

    @Test
    void addDocument_shouldRedirectBackToForm_whenFileIsEmpty() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "",
                "application/pdf",
                new byte[0]
        );

        LegalCase legalCase = new LegalCase();
        when(legalCaseService.getCaseById(1L)).thenReturn(legalCase);

        mockMvc.perform(multipart("/cases/1/documents").file(emptyFile))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases/1/documents/new"))
                .andExpect(flash().attribute("errorMessage", "Please select a file to upload."));

        verify(legalCaseService).getCaseById(1L);
        verify(documentService, never()).addDocumentToCase(any(), any());
    }

    @Test
    void addDocument_shouldRedirectToCaseDetails_whenUploadSucceeds() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contract.pdf",
                "application/pdf",
                "content".getBytes()
        );

        LegalCase legalCase = new LegalCase();
        when(legalCaseService.getCaseById(1L)).thenReturn(legalCase);

        mockMvc.perform(multipart("/cases/1/documents").file(file))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases/1"))
                .andExpect(flash().attribute("successMessage", "Document uploaded successfully."));

        verify(legalCaseService).getCaseById(1L);
        verify(documentService).addDocumentToCase(eq(1L), any());
    }

    @Test
    void addDocument_shouldRedirectBackToForm_whenIllegalArgumentExceptionOccurs() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "bad.exe",
                "application/octet-stream",
                "content".getBytes()
        );

        LegalCase legalCase = new LegalCase();
        when(legalCaseService.getCaseById(1L)).thenReturn(legalCase);

        doThrow(new IllegalArgumentException("File type is not allowed."))
                .when(documentService).addDocumentToCase(eq(1L), any());

        mockMvc.perform(multipart("/cases/1/documents").file(file))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases/1/documents/new"))
                .andExpect(flash().attribute("errorMessage", "File type is not allowed."));

        verify(documentService).addDocumentToCase(eq(1L), any());
    }

    @Test
    void addDocument_shouldRedirectBackToForm_whenIOExceptionOccurs() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contract.pdf",
                "application/pdf",
                "content".getBytes()
        );

        LegalCase legalCase = new LegalCase();
        when(legalCaseService.getCaseById(1L)).thenReturn(legalCase);

        doThrow(new IOException("Disk error"))
                .when(documentService).addDocumentToCase(eq(1L), any());

        mockMvc.perform(multipart("/cases/1/documents").file(file))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases/1/documents/new"))
                .andExpect(flash().attribute("errorMessage", "Failed to upload document."));

        verify(documentService).addDocumentToCase(eq(1L), any());
    }

    @Test
    void downloadDocument_shouldReturnNotFound_whenDocumentDoesNotExist() throws Exception {
        when(documentService.getDocumentById(1L)).thenReturn(null);

        mockMvc.perform(get("/documents/1/download"))
                .andExpect(status().isNotFound());

        verify(documentService).getDocumentById(1L);
    }

    @Test
    void downloadDocument_shouldReturnNotFound_whenFileDoesNotExist() throws Exception {
        Document document = new Document();
        document.setOriginalFileName("contract.pdf");
        document.setContentType("application/pdf");

        when(documentService.getDocumentById(1L)).thenReturn(document);
        when(documentService.getDocumentPath(document)).thenReturn(Path.of("non-existent-file.pdf"));

        mockMvc.perform(get("/documents/1/download"))
                .andExpect(status().isNotFound());

        verify(documentService).getDocumentById(1L);
        verify(documentService).getDocumentPath(document);
    }

    @Test
    void deleteDocument_shouldRedirectToCases_whenDocumentDoesNotExist() throws Exception {
        when(documentService.getDocumentById(1L)).thenReturn(null);

        mockMvc.perform(post("/documents/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases"));

        verify(documentService).getDocumentById(1L);
        verify(documentService, never()).deleteDocument(anyLong());
    }

    @Test
    void deleteDocument_shouldDeleteAndRedirectToCaseDetails_whenDocumentExists() throws Exception {
        LegalCase legalCase = mock(LegalCase.class);
        when(legalCase.getId()).thenReturn(5L);

        Document document = new Document();
        document.setLegalCase(legalCase);

        when(documentService.getDocumentById(1L)).thenReturn(document);

        mockMvc.perform(post("/documents/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases/5"))
                .andExpect(flash().attribute("successMessage", "Document deleted successfully."));

        verify(documentService).getDocumentById(1L);
        verify(documentService).deleteDocument(1L);
    }
}
