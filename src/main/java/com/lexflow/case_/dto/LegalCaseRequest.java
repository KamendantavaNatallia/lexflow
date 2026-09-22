package com.lexflow.case_.dto;

import com.lexflow.case_.model.CaseStatus;
import com.lexflow.case_.model.CaseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating or updating a legal case via the REST API.
 * The client never sends an id: the server generates it on create
 * and takes it from the URL on update.
 */
public record LegalCaseRequest(

        @NotBlank(message = "Case title is required")
        @Size(max = 255, message = "Case title must be at most 255 characters")
        String title,

        @NotBlank(message = "Client name is required")
        @Size(max = 255, message = "Client name must be at most 255 characters")
        String client,

        @NotNull(message = "Case type is required")
        CaseType type,

        @NotNull(message = "Case status is required")
        CaseStatus status
) {
}
