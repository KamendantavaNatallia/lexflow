package com.lexflow.note.controller;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.service.LegalCaseService;
import com.lexflow.note.model.Note;
import com.lexflow.note.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class NoteController {

    private final NoteService noteService;
    private final LegalCaseService legalCaseService;

    public NoteController(NoteService noteService,
                          LegalCaseService legalCaseService) {
        this.noteService = noteService;
        this.legalCaseService = legalCaseService;
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
}