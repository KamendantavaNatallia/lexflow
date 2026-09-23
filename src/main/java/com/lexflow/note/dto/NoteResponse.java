package com.lexflow.note.dto;

public record NoteResponse(
        Long id,
        Long caseId,
        String content
) {
}
