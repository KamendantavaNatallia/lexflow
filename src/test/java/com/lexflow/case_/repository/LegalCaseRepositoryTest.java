package com.lexflow.case_.repository;

import com.lexflow.case_.model.CaseStatus;
import com.lexflow.case_.model.CaseType;
import com.lexflow.case_.model.LegalCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LegalCaseRepositoryTest {

    @Autowired
    private LegalCaseRepository legalCaseRepository;

    @Test
    void findByTitleContainingIgnoreCaseOrClientContainingIgnoreCase_shouldReturnMatchingCases() {
        LegalCase case1 = new LegalCase();
        case1.setTitle("Contract Review");
        case1.setClient("Acme Corp");
        case1.setType(CaseType.CONTRACT);
        case1.setStatus(CaseStatus.OPEN);

        LegalCase case2 = new LegalCase();
        case2.setTitle("Compliance Audit");
        case2.setClient("Beta Ltd");
        case2.setType(CaseType.COMPLIANCE);
        case2.setStatus(CaseStatus.CLOSED);

        legalCaseRepository.save(case1);
        legalCaseRepository.save(case2);

        Page<LegalCase> result = legalCaseRepository
                .findByTitleContainingIgnoreCaseOrClientContainingIgnoreCase(
                        "contract",
                        "contract",
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
        assertEquals("Contract Review", result.getContent().get(0).getTitle());
    }

    @Test
    void findByStatus_shouldReturnCasesWithGivenStatus() {
        LegalCase openCase = new LegalCase();
        openCase.setTitle("Open Case");
        openCase.setClient("Client A");
        openCase.setType(CaseType.CONTRACT);
        openCase.setStatus(CaseStatus.OPEN);

        LegalCase closedCase = new LegalCase();
        closedCase.setTitle("Closed Case");
        closedCase.setClient("Client B");
        closedCase.setType(CaseType.CORPORATE);
        closedCase.setStatus(CaseStatus.CLOSED);

        legalCaseRepository.save(openCase);
        legalCaseRepository.save(closedCase);

        Page<LegalCase> result = legalCaseRepository.findByStatus(
                CaseStatus.OPEN,
                PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Open Case", result.getContent().get(0).getTitle());
        assertEquals(CaseStatus.OPEN, result.getContent().get(0).getStatus());
    }

    @Test
    void findByStatusAndTitleContainingIgnoreCaseOrStatusAndClientContainingIgnoreCase_shouldMatchStatusAndKeyword() {
        LegalCase matchingCase = new LegalCase();
        matchingCase.setTitle("Corporate Contract");
        matchingCase.setClient("Acme Corp");
        matchingCase.setType(CaseType.CORPORATE);
        matchingCase.setStatus(CaseStatus.OPEN);

        LegalCase wrongStatusCase = new LegalCase();
        wrongStatusCase.setTitle("Corporate Contract");
        wrongStatusCase.setClient("Other Client");
        wrongStatusCase.setType(CaseType.CORPORATE);
        wrongStatusCase.setStatus(CaseStatus.CLOSED);

        LegalCase wrongKeywordCase = new LegalCase();
        wrongKeywordCase.setTitle("Litigation Matter");
        wrongKeywordCase.setClient("Different Client");
        wrongKeywordCase.setType(CaseType.LITIGATION);
        wrongKeywordCase.setStatus(CaseStatus.OPEN);

        legalCaseRepository.save(matchingCase);
        legalCaseRepository.save(wrongStatusCase);
        legalCaseRepository.save(wrongKeywordCase);

        Page<LegalCase> result = legalCaseRepository
                .findByStatusAndTitleContainingIgnoreCaseOrStatusAndClientContainingIgnoreCase(
                        CaseStatus.OPEN, "Acme",
                        CaseStatus.OPEN, "Acme",
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
        assertEquals("Corporate Contract", result.getContent().get(0).getTitle());
        assertEquals("Acme Corp", result.getContent().get(0).getClient());
        assertEquals(CaseStatus.OPEN, result.getContent().get(0).getStatus());
    }
}
