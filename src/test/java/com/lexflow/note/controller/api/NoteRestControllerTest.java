package com.lexflow.note.controller.api;

import com.lexflow.case_.model.CaseStatus;
import com.lexflow.case_.model.CaseType;
import com.lexflow.case_.model.LegalCase;
import com.lexflow.common.config.SecurityConfig;
import com.lexflow.common.exception.ResourceNotFoundException;
import com.lexflow.note.mapper.NoteMapper;
import com.lexflow.note.model.Note;
import com.lexflow.note.service.NoteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NoteRestController.class)
@Import({SecurityConfig.class, NoteMapper.class})
class NoteRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteService noteService;

    private static Note note(Long id, String content) {
        LegalCase legalCase = new LegalCase("Supply dispute", "Acme LLC", CaseType.LITIGATION, CaseStatus.OPEN);
        ReflectionTestUtils.setField(legalCase, "id", 1L);

        Note note = new Note(content);
        ReflectionTestUtils.setField(note, "id", id);
        note.setLegalCase(legalCase);
        return note;
    }

    @Test
    @DisplayName("USER lists notes of a case")
    void user_shouldListNotes() throws Exception {
        when(noteService.getNotesForCase(1L)).thenReturn(List.of(note(5L, "Client prefers settlement")));

        mockMvc.perform(get("/api/v1/cases/1/notes").with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].caseId").value(1))
                .andExpect(jsonPath("$[0].content").value("Client prefers settlement"));
    }

    @Test
    @DisplayName("ADMIN creates a note: 201 with Location /api/v1/notes/{id}; content is trimmed")
    void admin_shouldCreateNote() throws Exception {
        when(noteService.createNote(eq(1L), any(Note.class))).thenReturn(note(6L, "Call the client"));

        mockMvc.perform(post("/api/v1/cases/1/notes")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "content": "  Call the client  " }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/notes/6")))
                .andExpect(jsonPath("$.content").value("Call the client"));

        verify(noteService).createNote(eq(1L), argThat(note -> note.getContent().equals("Call the client")));
    }

    @Test
    @DisplayName("Blank note returns 400; too long note returns 400")
    void createInvalidNote_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/cases/1/notes")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "content": "   " }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.content").value("Note content is required"));

        String tooLong = "a".repeat(2001);
        mockMvc.perform(post("/api/v1/cases/1/notes")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"content\": \"" + tooLong + "\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.content").value("Note content must be at most 2000 characters"));

        verify(noteService, never()).createNote(any(), any());
    }

    @Test
    @DisplayName("USER cannot update a note")
    void user_shouldBeForbidden_fromUpdate() throws Exception {
        mockMvc.perform(put("/api/v1/notes/5")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "content": "Changed" }
                                """))
                .andExpect(status().isForbidden());

        verify(noteService, never()).updateNote(any(), any());
    }

    @Test
    @DisplayName("ADMIN updates a note")
    void admin_shouldUpdateNote() throws Exception {
        when(noteService.updateNote(eq(5L), any(Note.class))).thenReturn(note(5L, "Changed"));

        mockMvc.perform(put("/api/v1/notes/5")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "content": "Changed" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Changed"));
    }

    @Test
    @DisplayName("Missing note returns 404")
    void getMissingNote_shouldReturnNotFound() throws Exception {
        when(noteService.getRequiredNote(99L)).thenThrow(new ResourceNotFoundException("Note not found with id: 99"));

        mockMvc.perform(get("/api/v1/notes/99").with(httpBasic("user", "user123")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Note not found with id: 99"));
    }

    @Test
    @DisplayName("ADMIN deletes a note: 204")
    void admin_shouldDeleteNote() throws Exception {
        mockMvc.perform(delete("/api/v1/notes/5").with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());

        verify(noteService).deleteRequiredNote(5L);
    }
}
