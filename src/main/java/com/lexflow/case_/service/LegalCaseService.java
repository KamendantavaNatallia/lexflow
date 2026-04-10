package com.lexflow.case_.service;

import com.lexflow.case_.model.CaseStatus;
import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.repository.LegalCaseRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LegalCaseService {

    private final LegalCaseRepository legalCaseRepository;

    public LegalCaseService(LegalCaseRepository legalCaseRepository) {
        this.legalCaseRepository = legalCaseRepository;
    }

    public List<LegalCase> getAllCases() {
        return legalCaseRepository.findAll();
    }

    public LegalCase getCaseById(Long id) {
        return legalCaseRepository.findById(id).orElse(null);
    }

    public void saveCase(LegalCase legalCase) {
        legalCaseRepository.save(legalCase);
    }

    public void deleteCase(Long id) {
        legalCaseRepository.deleteById(id);
    }

    public long getTotalCasesCount() {
        return legalCaseRepository.count();
    }

    public long getOpenCasesCount() {
        return legalCaseRepository.findAll()
                .stream()
                .filter(legalCase -> legalCase.getStatus() == CaseStatus.OPEN)
                .count();
    }

    public List<LegalCase> searchCases(String keyword, String status, String sort) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        String normalizedStatus = (status == null || status.isBlank()) ? "ALL" : status;
        String normalizedSort = (sort == null || sort.isBlank()) ? "newest" : sort;

        Sort sortOrder = buildSort(normalizedSort);

        boolean hasKeyword = !normalizedKeyword.isBlank();
        boolean hasStatus = !"ALL".equalsIgnoreCase(normalizedStatus);

        if (hasKeyword && hasStatus) {
            return legalCaseRepository.findByStatusAndTitleContainingIgnoreCaseOrStatusAndClientContainingIgnoreCase(
                    normalizedStatus, normalizedKeyword,
                    normalizedStatus, normalizedKeyword,
                    sortOrder
            );
        }

        if (hasKeyword) {
            return legalCaseRepository.findByTitleContainingIgnoreCaseOrClientContainingIgnoreCase(
                    normalizedKeyword, normalizedKeyword, sortOrder
            );
        }

        if (hasStatus) {
            return legalCaseRepository.findByStatus(normalizedStatus, sortOrder);
        }

        return legalCaseRepository.findAll(sortOrder);
    }

    public List<LegalCase> searchCases(String keyword, String status) {
        return searchCases(keyword, status, "newest");
    }

    private Sort buildSort(String sort) {
        return switch (sort) {
            case "title_asc" -> Sort.by(Sort.Direction.ASC, "title");
            case "status_asc" -> Sort.by(Sort.Direction.ASC, "status");
            case "newest" -> Sort.by(Sort.Direction.DESC, "id");
            default -> Sort.by(Sort.Direction.DESC, "id");
        };
    }
}