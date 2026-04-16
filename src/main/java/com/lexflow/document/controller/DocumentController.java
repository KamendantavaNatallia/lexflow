package com.lexflow.document.controller;

import com.lexflow.document.model.Document;
import com.lexflow.document.service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Controller
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public String uploadDocument(@RequestParam("caseId") Long caseId,
                                 @RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes) throws IOException {
        documentService.saveDocument(caseId, file);
        redirectAttributes.addFlashAttribute("successMessage", "Document uploaded successfully.");
        return "redirect:/cases/" + caseId;
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) throws MalformedURLException {
        Optional<Document> optionalDocument = documentService.getDocumentById(id);

        if (optionalDocument.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Document document = optionalDocument.get();
        Path filePath = documentService.getFilePath(document.getStoredFileName());

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.getOriginalFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, document.getContentType())
                .body(resource);
    }

    @PostMapping("/delete/{id}")
    public String deleteDocument(@PathVariable Long id, RedirectAttributes redirectAttributes) throws IOException {
        Optional<Document> optionalDocument = documentService.getDocumentById(id);

        if (optionalDocument.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Document not found.");
            return "redirect:/cases";
        }

        Document document = optionalDocument.get();
        Long caseId = document.getLegalCase().getId();

        documentService.deleteDocument(id);
        redirectAttributes.addFlashAttribute("successMessage", "Document deleted successfully.");

        return "redirect:/cases/" + caseId;
    }
}