package com.lexflow.case_.mapper;

import com.lexflow.case_.dto.LegalCaseRequest;
import com.lexflow.case_.dto.LegalCaseResponse;
import com.lexflow.case_.model.LegalCase;
import org.springframework.stereotype.Component;

@Component
public class LegalCaseMapper {

    public LegalCaseResponse toResponse(LegalCase legalCase) {
        return new LegalCaseResponse(
                legalCase.getId(),
                legalCase.getTitle(),
                legalCase.getClient(),
                legalCase.getType(),
                legalCase.getStatus()
        );
    }

    public LegalCase toEntity(LegalCaseRequest request) {
        return new LegalCase(
                request.title().trim(),
                request.client().trim(),
                request.type(),
                request.status()
        );
    }
}
