package com.lexflow.note.service;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.repository.LegalCaseRepository;
import com.lexflow.common.exception.ResourceNotFoundException;
import com.lexflow.note.model.Note;
import com.lexflow.note.repository.NoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        return noteRepository.save(note);
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

    // ---------- REST API methods: missing entities throw ResourceNotFoundException (HTTP 404) ----------

    /**
     * Newest notes first (highest id first, since notes have no timestamp yet).
     */
    @Transactional(readOnly = true)
    public List<Note> getNotesForCase(Long caseId) {
        if (!legalCaseRepository.existsById(caseId)) {
            throw new ResourceNotFoundException("Legal case not found with id: " + caseId);
        }
        return noteRepository.findByLegalCaseIdOrderByIdDesc(caseId);
    }

    @Transactional
    public Note createNote(Long caseId, Note note) {
        LegalCase legalCase = legalCaseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Legal case not found with id: " + caseId));

        note.setLegalCase(legalCase);
        return noteRepository.save(note);
    }

    public Note getRequiredNote(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));
    }

    @Transactional
    public Note updateNote(Long id, Note changes) {
        Note existing = getRequiredNote(id);
        existing.setContent(changes.getContent());
        return existing;
    }

    @Transactional
    public void deleteRequiredNote(Long id) {
        Note existing = getRequiredNote(id);
        noteRepository.delete(existing);
    }
}
