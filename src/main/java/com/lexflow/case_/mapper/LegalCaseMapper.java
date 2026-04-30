package com.lexflow.case_.mapper;

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
}