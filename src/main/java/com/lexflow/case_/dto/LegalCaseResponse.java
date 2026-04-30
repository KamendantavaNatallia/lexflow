package com.lexflow.case_.dto;

import com.lexflow.case_.model.CaseStatus;
import com.lexflow.case_.model.CaseType;

public record LegalCaseResponse(
        Long id,
        String title,
        String client,
        CaseType type,
        CaseStatus status
) {
}