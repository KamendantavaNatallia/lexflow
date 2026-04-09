package org.example.service;

import org.example.model.LegalCase;
import org.example.model.Note;
import org.example.repository.LegalCaseRepository;
import org.example.repository.NoteRepository;
import org.springframework.stereotype.Service;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final LegalCaseRepository legalCaseRepository;

    public NoteService(NoteRepository noteRepository,
                       LegalCaseRepository legalCaseRepository) {
        this.noteRepository = noteRepository;
        this.legalCaseRepository = legalCaseRepository;
    }

    public Note addNoteToCase(Long caseId, Note note) {
        LegalCase legalCase = legalCaseRepository.findById(caseId).orElse(null);
        if (legalCase == null) {
            return null;
        }

        note.setLegalCase(legalCase);
        legalCase.getNotes().add(note);
        legalCaseRepository.save(legalCase);

        return note;
    }

    public Note saveNote(Note note) {
        return noteRepository.save(note);
    }

    public Note getNoteById(Long id) {
        return noteRepository.findById(id).orElse(null);
    }

    public void deleteNote(Long id) {
        Note note = noteRepository.findById(id).orElse(null);
        if (note != null) {
            noteRepository.delete(note);
        }
    }
}