package com.lexflow.case_.service;

import com.lexflow.case_.model.CaseStatus;
import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.repository.LegalCaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public Page<LegalCase> searchCases(String keyword, String status, String sort, int page, int size) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        String normalizedStatus = (status == null || status.isBlank()) ? "ALL" : status;
        String normalizedSort = (sort == null || sort.isBlank()) ? "newest" : sort;

        Pageable pageable = PageRequest.of(page, size, buildSort(normalizedSort));

        boolean hasKeyword = !normalizedKeyword.isBlank();
        boolean hasStatus = !"ALL".equalsIgnoreCase(normalizedStatus);

        if (hasKeyword && hasStatus) {
            CaseStatus caseStatus = CaseStatus.valueOf(normalizedStatus.toUpperCase());
            return legalCaseRepository.findByStatusAndTitleContainingIgnoreCaseOrStatusAndClientContainingIgnoreCase(
                    caseStatus, normalizedKeyword,
                    caseStatus, normalizedKeyword,
                    pageable
            );
        }

        if (hasKeyword) {
            return legalCaseRepository.findByTitleContainingIgnoreCaseOrClientContainingIgnoreCase(
                    normalizedKeyword, normalizedKeyword, pageable
            );
        }

        if (hasStatus) {
            CaseStatus caseStatus = CaseStatus.valueOf(normalizedStatus.toUpperCase());
            return legalCaseRepository.findByStatus(caseStatus, pageable);
        }

        return legalCaseRepository.findAll(pageable);
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