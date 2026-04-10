package com.lexflow.deadline.controller;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.service.LegalCaseService;
import com.lexflow.deadline.model.Deadline;
import com.lexflow.deadline.service.DeadlineService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DeadlineController {

    private final DeadlineService deadlineService;
    private final LegalCaseService legalCaseService;

    public DeadlineController(DeadlineService deadlineService,
                              LegalCaseService legalCaseService) {
        this.deadlineService = deadlineService;
        this.legalCaseService = legalCaseService;
    }

    @GetMapping("/cases/{id}/deadlines/new")
    public String showDeadlineForm(@PathVariable Long id, Model model) {
        LegalCase selectedCase = legalCaseService.getCaseById(id);
        if (selectedCase == null) {
            return "redirect:/cases";
        }

        model.addAttribute("legalCase", selectedCase);
        model.addAttribute("deadline", new Deadline());
        model.addAttribute("formMode", "create");
        return "deadline-form";
    }

    @PostMapping("/cases/{id}/deadlines")
    public String addDeadline(@PathVariable Long id,
                              @Valid @ModelAttribute Deadline deadline,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        LegalCase selectedCase = legalCaseService.getCaseById(id);
        if (selectedCase == null) {
            return "redirect:/cases";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("legalCase", selectedCase);
            model.addAttribute("formMode", "create");
            return "deadline-form";
        }

        deadlineService.addDeadlineToCase(id, deadline);
        redirectAttributes.addFlashAttribute("successMessage", "Deadline created successfully.");
        return "redirect:/cases/" + id;
    }

    @GetMapping("/deadlines/{id}/edit")
    public String showEditDeadlineForm(@PathVariable Long id, Model model) {
        Deadline deadline = deadlineService.getDeadlineById(id);
        if (deadline == null) {
            return "redirect:/cases";
        }

        model.addAttribute("deadline", deadline);
        model.addAttribute("legalCase", deadline.getLegalCase());
        model.addAttribute("formMode", "edit");
        return "deadline-form";
    }

    @PostMapping("/deadlines/{id}/edit")
    public String updateDeadline(@PathVariable Long id,
                                 @Valid @ModelAttribute Deadline deadline,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        Deadline existingDeadline = deadlineService.getDeadlineById(id);
        if (existingDeadline == null) {
            return "redirect:/cases";
        }

        LegalCase legalCase = existingDeadline.getLegalCase();

        if (bindingResult.hasErrors()) {
            model.addAttribute("legalCase", legalCase);
            model.addAttribute("formMode", "edit");
            return "deadline-form";
        }

        existingDeadline.setTitle(deadline.getTitle());
        existingDeadline.setDueDate(deadline.getDueDate());
        existingDeadline.setPriority(deadline.getPriority());

        deadlineService.saveDeadline(existingDeadline);
        redirectAttributes.addFlashAttribute("successMessage", "Deadline updated successfully.");
        return "redirect:/cases/" + legalCase.getId();
    }

    @PostMapping("/deadlines/{id}/complete")
    public String markDeadlineCompleted(@PathVariable Long id,
                                        RedirectAttributes redirectAttributes) {
        Deadline deadline = deadlineService.getDeadlineById(id);
        if (deadline == null) {
            return "redirect:/cases";
        }

        Long caseId = deadline.getLegalCase().getId();
        deadlineService.markCompleted(id);
        redirectAttributes.addFlashAttribute("successMessage", "Deadline marked as completed.");

        return "redirect:/cases/" + caseId;
    }

    @PostMapping("/deadlines/{id}/delete")
    public String deleteDeadline(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        Deadline deadline = deadlineService.getDeadlineById(id);
        if (deadline == null) {
            return "redirect:/cases";
        }

        Long caseId = deadline.getLegalCase().getId();
        deadlineService.deleteDeadline(id);
        redirectAttributes.addFlashAttribute("successMessage", "Deadline deleted successfully.");

        return "redirect:/cases/" + caseId;
    }
}