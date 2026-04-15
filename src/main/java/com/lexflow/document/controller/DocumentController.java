package com.lexflow.document.controller;

import com.lexflow.document.model.Document;
import com.lexflow.document.service.DocumentService;
import jakarta.servlet.ServletException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
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
                                 RedirectAttributes redirectAttributes) {
        try {
            documentService.saveDocument(caseId, file);
            redirectAttributes.addFlashAttribute("successMessage", "Document uploaded successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload document.");
        }

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
    public String deleteDocument(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
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
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete document.");
            return "redirect:/cases";
        }
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "File is too large.");
        return "redirect:/cases";
    }

    @ExceptionHandler(ServletException.class)
    public String handleServletException(ServletException e, RedirectAttributes redirectAttributes) {
        Throwable rootCause = e.getRootCause();

        if (rootCause instanceof SizeLimitExceededException) {
            redirectAttributes.addFlashAttribute("errorMessage", "File is too large.");
            return "redirect:/cases";
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Upload failed.");
        return "redirect:/cases";
    }
}