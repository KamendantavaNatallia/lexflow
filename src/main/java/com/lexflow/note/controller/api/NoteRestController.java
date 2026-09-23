package com.lexflow.note.controller.api;

import com.lexflow.note.dto.NoteRequest;
import com.lexflow.note.dto.NoteResponse;
import com.lexflow.note.mapper.NoteMapper;
import com.lexflow.note.model.Note;
import com.lexflow.note.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST API for case notes. Same URL design as deadlines:
 * list and create under the case, read/update/delete by the note's own id.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Notes", description = "Free-text notes attached to a case")
public class NoteRestController {

    private final NoteService noteService;
    private final NoteMapper noteMapper;

    public NoteRestController(NoteService noteService, NoteMapper noteMapper) {
        this.noteService = noteService;
        this.noteMapper = noteMapper;
    }

    @GetMapping("/cases/{caseId}/notes")
    @Operation(summary = "List notes of a case, newest first")
    public List<NoteResponse> getNotesForCase(@PathVariable Long caseId) {
        return noteService.getNotesForCase(caseId).stream()
                .map(noteMapper::toResponse)
                .toList();
    }

    @PostMapping("/cases/{caseId}/notes")
    @Operation(summary = "Add a note to a case (ADMIN)")
    public ResponseEntity<NoteResponse> createNote(
            @PathVariable Long caseId,
            @Valid @RequestBody NoteRequest request
    ) {
        Note created = noteService.createNote(caseId, noteMapper.toEntity(request));

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/notes/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(noteMapper.toResponse(created));
    }

    @GetMapping("/notes/{id}")
    @Operation(summary = "Get one note")
    public NoteResponse getNote(@PathVariable Long id) {
        return noteMapper.toResponse(noteService.getRequiredNote(id));
    }

    @PutMapping("/notes/{id}")
    @Operation(summary = "Replace the text of a note (ADMIN)")
    public NoteResponse updateNote(@PathVariable Long id, @Valid @RequestBody NoteRequest request) {
        return noteMapper.toResponse(noteService.updateNote(id, noteMapper.toEntity(request)));
    }

    @DeleteMapping("/notes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a note (ADMIN)")
    public void deleteNote(@PathVariable Long id) {
        noteService.deleteRequiredNote(id);
    }
}
