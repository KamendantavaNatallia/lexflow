package com.lexflow.document.controller;

import com.lexflow.case_.service.LegalCaseService;
import com.lexflow.document.service.DocumentService;
import org.springframework.stereotype.Controller;

@Controller
public class DocumentController {

    private final DocumentService documentService;
    private final LegalCaseService legalCaseService;

    public DocumentController(DocumentService documentService, LegalCaseService legalCaseService) {
        this.documentService = documentService;
        this.legalCaseService = legalCaseService;
    }
}
