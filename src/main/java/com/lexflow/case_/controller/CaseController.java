package com.lexflow.case_.controller;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.service.LegalCaseService;
import com.lexflow.deadline.model.Deadline;
import com.lexflow.deadline.service.DeadlineService;
import com.lexflow.document.model.Document;
import com.lexflow.document.service.DocumentService;
import com.lexflow.note.model.Note;
import com.lexflow.note.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CaseController {

    private final LegalCaseService legalCaseService;
    private final DeadlineService deadlineService;
    private final NoteService noteService;
    private final DocumentService documentService;

    public CaseController(LegalCaseService legalCaseService,
                          DeadlineService deadlineService,
                          NoteService noteService,
                          DocumentService documentService) {
        this.legalCaseService = legalCaseService;
        this.deadlineService = deadlineService;
        this.noteService = noteService;
        this.documentService = documentService;
    }

    @GetMapping("/cases")
    public String cases(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false, defaultValue = "ALL") String status,
                        Model model) {
        model.addAttribute("cases", legalCaseService.searchCases(keyword, status));
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("selectedStatus", status == null || status.isBlank() ? "ALL" : status);
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

    @GetMapping("/cases/{id}/notes/new")
    public String showNoteForm(@PathVariable Long id, Model model) {
        LegalCase selectedCase = legalCaseService.getCaseById(id);
        if (selectedCase == null) {
            return "redirect:/cases";
        }

        model.addAttribute("legalCase", selectedCase);
        model.addAttribute("note", new Note());
        model.addAttribute("formMode", "create");
        return "note-form";
    }

    @PostMapping("/cases/{id}/notes")
    public String addNote(@PathVariable Long id,
                          @Valid @ModelAttribute Note note,
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
            return "note-form";
        }

        noteService.addNoteToCase(id, note);
        redirectAttributes.addFlashAttribute("successMessage", "Note added successfully.");
        return "redirect:/cases/" + id;
    }

    @GetMapping("/notes/{id}/edit")
    public String showEditNoteForm(@PathVariable Long id, Model model) {
        Note note = noteService.getNoteById(id);
        if (note == null) {
            return "redirect:/cases";
        }

        model.addAttribute("note", note);
        model.addAttribute("legalCase", note.getLegalCase());
        model.addAttribute("formMode", "edit");
        return "note-form";
    }

    @PostMapping("/notes/{id}/edit")
    public String updateNote(@PathVariable Long id,
                             @Valid @ModelAttribute Note note,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Note existingNote = noteService.getNoteById(id);
        if (existingNote == null) {
            return "redirect:/cases";
        }

        LegalCase legalCase = existingNote.getLegalCase();

        if (bindingResult.hasErrors()) {
            model.addAttribute("legalCase", legalCase);
            model.addAttribute("formMode", "edit");
            return "note-form";
        }

        existingNote.setContent(note.getContent());
        noteService.saveNote(existingNote);
        redirectAttributes.addFlashAttribute("successMessage", "Note updated successfully.");

        return "redirect:/cases/" + legalCase.getId();
    }

    @PostMapping("/notes/{id}/delete")
    public String deleteNote(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        Note note = noteService.getNoteById(id);
        if (note == null) {
            return "redirect:/cases";
        }

        Long caseId = note.getLegalCase().getId();
        noteService.deleteNote(id);
        redirectAttributes.addFlashAttribute("successMessage", "Note deleted successfully.");

        return "redirect:/cases/" + caseId;
    }

    @GetMapping("/cases/{id}/documents/new")
    public String showDocumentForm(@PathVariable Long id, Model model) {
        LegalCase selectedCase = legalCaseService.getCaseById(id);
        if (selectedCase == null) {
            return "redirect:/cases";
        }

        model.addAttribute("legalCase", selectedCase);
        model.addAttribute("document", new Document());
        return "document-form";
    }

    @PostMapping("/cases/{id}/documents")
    public String addDocument(@PathVariable Long id,
                              @Valid @ModelAttribute Document document,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        LegalCase selectedCase = legalCaseService.getCaseById(id);
        if (selectedCase == null) {
            return "redirect:/cases";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("legalCase", selectedCase);
            return "document-form";
        }

        documentService.addDocumentToCase(id, document);
        redirectAttributes.addFlashAttribute("successMessage", "Document saved successfully.");
        return "redirect:/cases/" + id;
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