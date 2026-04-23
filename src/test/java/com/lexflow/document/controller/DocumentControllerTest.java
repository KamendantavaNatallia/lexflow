package com.lexflow.document.controller;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.document.model.Document;
import com.lexflow.document.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @Test
    void uploadDocument_shouldRedirectToCase_whenUploadSucceeds() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contract.pdf",
                "application/pdf",
                "content".getBytes()
        );

        doNothing().when(documentService).saveDocument(1L, file);

        mockMvc.perform(multipart("/documents/upload")
                        .file(file)
                        .param("caseId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases/1"))
                .andExpect(flash().attribute("successMessage", "Document uploaded successfully."));

        verify(documentService).saveDocument(1L, file);
    }

    @Test
    void uploadDocument_shouldRedirectToCases_whenIllegalArgumentExceptionOccurs() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "bad.exe",
                "application/octet-stream",
                "content".getBytes()
        );

        doThrow(new IllegalArgumentException("Unsupported file type."))
                .when(documentService).saveDocument(1L, file);

        mockMvc.perform(multipart("/documents/upload")
                        .file(file)
                        .param("caseId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases"))
                .andExpect(flash().attribute("errorMessage", "Unsupported file type."));

        verify(documentService).saveDocument(1L, file);
    }

    @Test
    void uploadDocument_shouldRedirectToCases_whenIOExceptionOccurs() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "contract.pdf",
                "application/pdf",
                "content".getBytes()
        );

        doThrow(new IOException("Disk error"))
                .when(documentService).saveDocument(1L, file);

        mockMvc.perform(multipart("/documents/upload")
                        .file(file)
                        .param("caseId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases"))
                .andExpect(flash().attribute("errorMessage", "File operation failed."));

        verify(documentService).saveDocument(1L, file);
    }

    @Test
    void downloadDocument_shouldReturnNotFound_whenDocumentDoesNotExist() throws Exception {
        when(documentService.getDocumentById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/documents/download/1"))
                .andExpect(status().isNotFound());

        verify(documentService).getDocumentById(1L);
    }

    @Test
    void downloadDocument_shouldReturnNotFound_whenFileDoesNotExist() throws Exception {
        Document document = new Document();
        document.setOriginalFileName("contract.pdf");
        document.setStoredFileName("stored-contract.pdf");
        document.setContentType("application/pdf");

        when(documentService.getDocumentById(1L)).thenReturn(Optional.of(document));
        when(documentService.getFilePath("stored-contract.pdf"))
                .thenReturn(Path.of("non-existent-file.pdf"));

        mockMvc.perform(get("/documents/download/1"))
                .andExpect(status().isNotFound());

        verify(documentService).getDocumentById(1L);
        verify(documentService).getFilePath("stored-contract.pdf");
    }

    @Test
    void deleteDocument_shouldRedirectToCases_whenDocumentDoesNotExist() throws Exception {
        when(documentService.getDocumentById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/documents/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases"))
                .andExpect(flash().attribute("errorMessage", "Document not found."));

        verify(documentService).getDocumentById(1L);
        verify(documentService, never()).deleteDocument(anyLong());
    }

    @Test
    void deleteDocument_shouldDeleteAndRedirectToCaseDetails_whenDocumentExists() throws Exception {
        LegalCase legalCase = mock(LegalCase.class);
        when(legalCase.getId()).thenReturn(5L);

        Document document = new Document();
        document.setLegalCase(legalCase);

        when(documentService.getDocumentById(1L)).thenReturn(Optional.of(document));

        mockMvc.perform(post("/documents/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases/5"))
                .andExpect(flash().attribute("successMessage", "Document deleted successfully."));

        verify(documentService).getDocumentById(1L);
        verify(documentService).deleteDocument(1L);
    }

    @Test
    void deleteDocument_shouldRedirectToCases_whenDeleteFails() throws Exception {
        LegalCase legalCase = mock(LegalCase.class);
        when(legalCase.getId()).thenReturn(5L);

        Document document = new Document();
        document.setLegalCase(legalCase);

        when(documentService.getDocumentById(1L)).thenReturn(Optional.of(document));
        doThrow(new IOException("Delete failed")).when(documentService).deleteDocument(1L);

        mockMvc.perform(post("/documents/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases"))
                .andExpect(flash().attribute("errorMessage", "File operation failed."));

        verify(documentService).getDocumentById(1L);
        verify(documentService).deleteDocument(1L);
    }
}