package org.example.service;

import org.example.model.CaseStatus;
import org.example.model.LegalCase;
import org.example.repository.LegalCaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LegalCaseService {

    private final LegalCaseRepository legalCaseRepository;

    public LegalCaseService(LegalCaseRepository legalCaseRepository) {
        this.legalCaseRepository = legalCaseRepository;
    }

    public List<LegalCase> getAllCases() {
        return legalCaseRepository.findAll();
    }

    public List<LegalCase> searchCases(String keyword, String status) {
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasStatus = status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status);

        List<LegalCase> cases = legalCaseRepository.findAll();

        if (hasStatus) {
            try {
                CaseStatus caseStatus = CaseStatus.valueOf(status.trim().toUpperCase());
                cases = cases.stream()
                        .filter(c -> c.getStatus() == caseStatus)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException ignored) {
                cases = List.of();
            }
        }

        if (hasKeyword) {
            String lowerKeyword = keyword.trim().toLowerCase();
            cases = cases.stream()
                    .filter(c ->
                            c.getTitle().toLowerCase().contains(lowerKeyword) ||
                                    c.getClient().toLowerCase().contains(lowerKeyword)
                    )
                    .collect(Collectors.toList());
        }

        return cases;
    }

    public long getTotalCasesCount() {
        return legalCaseRepository.count();
    }

    public long getOpenCasesCount() {
        return legalCaseRepository.findAll()
                .stream()
                .filter(c -> c.getStatus() == CaseStatus.OPEN)
                .count();
    }

    public LegalCase getCaseById(Long id) {
        return legalCaseRepository.findById(id).orElse(null);
    }

    public LegalCase saveCase(LegalCase legalCase) {
        return legalCaseRepository.save(legalCase);
    }

    public void deleteCase(Long id) {
        if (legalCaseRepository.existsById(id)) {
            legalCaseRepository.deleteById(id);
        }
    }
}
