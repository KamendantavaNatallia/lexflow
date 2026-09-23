package com.lexflow.note.mapper;

import com.lexflow.note.dto.NoteRequest;
import com.lexflow.note.dto.NoteResponse;
import com.lexflow.note.model.Note;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {

    public NoteResponse toResponse(Note note) {
        Long caseId = note.getLegalCase() == null ? null : note.getLegalCase().getId();
        return new NoteResponse(note.getId(), caseId, note.getContent());
    }

    public Note toEntity(NoteRequest request) {
        return new Note(request.content().trim());
    }
}
