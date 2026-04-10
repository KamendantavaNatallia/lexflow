package com.lexflow.document.controller;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.service.LegalCaseService;
import com.lexflow.document.model.Document;
import com.lexflow.document.service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;

@Controller
public class DocumentController {

    private final DocumentService documentService;
    private final LegalCaseService legalCaseService;

    public DocumentController(DocumentService documentService,
                              LegalCaseService legalCaseService) {
        this.documentService = documentService;
        this.legalCaseService = legalCaseService;
    }

    @GetMapping("/cases/{id}/documents/new")
    public String showDocumentForm(@PathVariable Long id, Model model) {
        LegalCase selectedCase = legalCaseService.getCaseById(id);
        if (selectedCase == null) {
            return "redirect:/cases";
        }

        model.addAttribute("legalCase", selectedCase);
        return "document-form";
    }

    @PostMapping("/cases/{id}/documents")
    public String addDocument(@PathVariable Long id,
                              @RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        LegalCase selectedCase = legalCaseService.getCaseById(id);
        if (selectedCase == null) {
            return "redirect:/cases";
        }

        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a file to upload.");
            return "redirect:/cases/" + id + "/documents/new";
        }

        try {
            documentService.addDocumentToCase(id, file);
            redirectAttributes.addFlashAttribute("successMessage", "Document uploaded successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cases/" + id + "/documents/new";
        } catch (MaxUploadSizeExceededException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "File is too large. Maximum size is 10 MB.");
            return "redirect:/cases/" + id + "/documents/new";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload document.");
            return "redirect:/cases/" + id + "/documents/new";
        }

        return "redirect:/cases/" + id;
    }

    @GetMapping("/documents/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) throws IOException {
        Document document = documentService.getDocumentById(id);
        if (document == null) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = documentService.getDocumentPath(document);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
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