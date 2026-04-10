package com.lexflow.case_.controller;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.service.LegalCaseService;
import com.lexflow.document.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CaseController {

    private final LegalCaseService legalCaseService;
    private final DocumentService documentService;

    public CaseController(LegalCaseService legalCaseService,
                          DocumentService documentService) {
        this.legalCaseService = legalCaseService;
        this.documentService = documentService;
    }

    @GetMapping("/cases")
    public String cases(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false, defaultValue = "ALL") String status,
                        @RequestParam(required = false, defaultValue = "newest") String sort,
                        Model model) {
        model.addAttribute("cases", legalCaseService.searchCases(keyword, status, sort));
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("selectedStatus", status == null || status.isBlank() ? "ALL" : status);
        model.addAttribute("selectedSort", sort == null || sort.isBlank() ? "newest" : sort);
        return "cases";
    }

    @GetMapping("/cases/new")
    public String showNewCaseForm(Model model) {
        model.addAttribute("legalCase", new LegalCase());
        model.addAttribute("formMode", "create");
        return "case-form";
    }

    @PostMapping("/cases")
    public String addCase(@Valid @ModelAttribute LegalCase legalCase,
                          BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formMode", "create");
            return "case-form";
        }

        legalCaseService.saveCase(legalCase);
        redirectAttributes.addFlashAttribute("successMessage", "Case created successfully.");
        return "redirect:/cases";
    }

    @GetMapping("/cases/{id}")
    public String caseDetails(@PathVariable Long id, Model model) {
        LegalCase selectedCase = legalCaseService.getCaseById(id);
        if (selectedCase == null) {
            return "redirect:/cases";
        }

        model.addAttribute("legalCase", selectedCase);
        model.addAttribute("documentService", documentService);
        return "case-details";
    }

    @GetMapping("/cases/{id}/edit")
    public String showEditCaseForm(@PathVariable Long id, Model model) {
        LegalCase selectedCase = legalCaseService.getCaseById(id);
        if (selectedCase == null) {
            return "redirect:/cases";
        }

        model.addAttribute("legalCase", selectedCase);
        model.addAttribute("formMode", "edit");
        return "case-form";
    }

    @PostMapping("/cases/{id}/edit")
    public String updateCase(@PathVariable Long id,
                             @Valid @ModelAttribute LegalCase legalCase,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        LegalCase existingCase = legalCaseService.getCaseById(id);
        if (existingCase == null) {
            return "redirect:/cases";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("formMode", "edit");
            return "case-form";
        }

        existingCase.setTitle(legalCase.getTitle());
        existingCase.setClient(legalCase.getClient());
        existingCase.setType(legalCase.getType());
        existingCase.setStatus(legalCase.getStatus());

        legalCaseService.saveCase(existingCase);
        redirectAttributes.addFlashAttribute("successMessage", "Case updated successfully.");
        return "redirect:/cases/" + id;
    }

    @PostMapping("/cases/{id}/delete")
    public String deleteCase(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        legalCaseService.deleteCase(id);
        redirectAttributes.addFlashAttribute("successMessage", "Case deleted successfully.");
        return "redirect:/cases";
    }
}