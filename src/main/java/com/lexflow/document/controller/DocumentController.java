package com.lexflow.document.controller;

import com.lexflow.document.model.Document;
import com.lexflow.document.service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;

@Controller
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/documents/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) throws IOException {
        Document document = documentService.getDocumentById(id);
        if (document == null) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = documentService.getDocumentPath(document);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                .body(resource);
    }

    @PostMapping("/documents/{id}/delete")
    public String deleteDocument(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        Document document = documentService.getDocumentById(id);
        if (document == null) {
            return "redirect:/cases";
        }

        Long caseId = document.getLegalCase().getId();
        documentService.deleteDocument(id);
        redirectAttributes.addFlashAttribute("successMessage", "Document deleted successfully.");

        return "redirect:/cases/" + caseId;
    }
}