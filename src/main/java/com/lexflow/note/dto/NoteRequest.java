package com.lexflow.note.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating or updating a note. The 2000-character limit matches the notes.content column.
 */
public record NoteRequest(

        @NotBlank(message = "Note content is required")
        @Size(max = 2000, message = "Note content must be at most 2000 characters")
        String content
) {
}
