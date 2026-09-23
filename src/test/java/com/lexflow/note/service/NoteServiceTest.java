package com.lexflow.note.service;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.repository.LegalCaseRepository;
import com.lexflow.common.exception.ResourceNotFoundException;
import com.lexflow.note.model.Note;
import com.lexflow.note.repository.NoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private LegalCaseRepository legalCaseRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void addNoteToCase_shouldReturnNull_whenCaseDoesNotExist() {
        Note note = new Note();
        note.setContent("Important note");

        when(legalCaseRepository.findById(1L)).thenReturn(Optional.empty());

        Note result = noteService.addNoteToCase(1L, note);

        assertNull(result);
        verify(legalCaseRepository).findById(1L);
        verify(noteRepository, never()).save(any());
        verify(legalCaseRepository, never()).save(any());
    }

    @Test
    void addNoteToCase_shouldAttachNoteToCaseAndSave_whenCaseExists() {
        LegalCase legalCase = new LegalCase();

        Note note = new Note();
        note.setContent("Important note");

        when(legalCaseRepository.findById(1L)).thenReturn(Optional.of(legalCase));
        when(noteRepository.save(note)).thenReturn(note);

        Note result = noteService.addNoteToCase(1L, note);

        assertNotNull(result);
        assertEquals(note, result);
        assertEquals(legalCase, note.getLegalCase());

        verify(legalCaseRepository).findById(1L);
        verify(noteRepository).save(note);
        verify(legalCaseRepository, never()).save(any());
    }

    @Test
    void saveNote_shouldCallRepositorySave() {
        Note note = new Note();
        note.setContent("Saved note");

        when(noteRepository.save(note)).thenReturn(note);

        Note result = noteService.saveNote(note);

        assertEquals(note, result);
        verify(noteRepository).save(note);
    }

    @Test
    void getNoteById_shouldReturnNote_whenFound() {
        Note note = new Note();
        note.setContent("Existing note");

        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        Note result = noteService.getNoteById(1L);

        assertNotNull(result);
        assertEquals("Existing note", result.getContent());
        verify(noteRepository).findById(1L);
    }

    @Test
    void getNoteById_shouldReturnNull_whenNotFound() {
        when(noteRepository.findById(999L)).thenReturn(Optional.empty());

        Note result = noteService.getNoteById(999L);

        assertNull(result);
        verify(noteRepository).findById(999L);
    }

    @Test
    void deleteNote_shouldDeleteNote_whenNoteExists() {
        Note note = new Note();
        note.setContent("Delete me");

        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        noteService.deleteNote(1L);

        verify(noteRepository).findById(1L);
        verify(noteRepository).delete(note);
    }

    @Test
    void deleteNote_shouldDoNothing_whenNoteDoesNotExist() {
        when(noteRepository.findById(999L)).thenReturn(Optional.empty());

        noteService.deleteNote(999L);

        verify(noteRepository).findById(999L);
        verify(noteRepository, never()).delete(any());
    }

    // ---------- REST API methods ----------

    @Test
    void getNotesForCase_shouldThrowNotFound_whenCaseDoesNotExist() {
        when(legalCaseRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> noteService.getNotesForCase(99L));
        verify(noteRepository, never()).findByLegalCaseIdOrderByIdDesc(any());
    }

    @Test
    void getNotesForCase_shouldReturnNotesOfCase() {
        when(legalCaseRepository.existsById(1L)).thenReturn(true);
        when(noteRepository.findByLegalCaseIdOrderByIdDesc(1L)).thenReturn(List.of(new Note("Call the client")));

        List<Note> result = noteService.getNotesForCase(1L);

        assertEquals(1, result.size());
        assertEquals("Call the client", result.get(0).getContent());
    }

    @Test
    void createNote_shouldAttachToCaseAndSave() {
        LegalCase legalCase = new LegalCase();
        Note note = new Note("Call the client");
        when(legalCaseRepository.findById(1L)).thenReturn(Optional.of(legalCase));
        when(noteRepository.save(note)).thenReturn(note);

        Note result = noteService.createNote(1L, note);

        assertSame(legalCase, result.getLegalCase());
        verify(noteRepository).save(note);
    }

    @Test
    void createNote_shouldThrowNotFound_whenCaseDoesNotExist() {
        when(legalCaseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> noteService.createNote(99L, new Note("Text")));
        verify(noteRepository, never()).save(any());
    }

    @Test
    void updateNote_shouldReplaceContent() {
        Note existing = new Note("Old text");
        when(noteRepository.findById(1L)).thenReturn(Optional.of(existing));

        Note result = noteService.updateNote(1L, new Note("New text"));

        assertSame(existing, result);
        assertEquals("New text", result.getContent());
    }

    @Test
    void deleteRequiredNote_shouldThrowNotFound_andNotDelete_whenNoteDoesNotExist() {
        when(noteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> noteService.deleteRequiredNote(99L));
        verify(noteRepository, never()).delete(any());
    }
}
