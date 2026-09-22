package com.lexflow.case_.service;

import com.lexflow.case_.model.CaseStatus;
import com.lexflow.case_.model.CaseType;
import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.repository.LegalCaseRepository;
import com.lexflow.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return legalCaseRepository.countByStatus(CaseStatus.OPEN);
    }

    public List<LegalCase> getRecentCases(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
        return legalCaseRepository.findAll(pageable).getContent();
    }

    /**
     * Unlike getCaseById (used by the Thymeleaf pages), this method never returns null:
     * a missing case is an error that the REST layer turns into HTTP 404.
     */
    public LegalCase getRequiredCase(Long id) {
        return legalCaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Legal case not found with id: " + id));
    }

    @Transactional
    public LegalCase createCase(LegalCase legalCase) {
        return legalCaseRepository.save(legalCase);
    }

    /**
     * Loads the managed entity and copies the new values into it.
     * No explicit save() is needed: inside @Transactional, Hibernate dirty checking
     * detects the changed fields and issues an UPDATE on commit.
     */
    @Transactional
    public LegalCase updateCase(Long id, LegalCase changes) {
        LegalCase existing = getRequiredCase(id);
        existing.setTitle(changes.getTitle());
        existing.setClient(changes.getClient());
        existing.setType(changes.getType());
        existing.setStatus(changes.getStatus());
        return existing;
    }

    @Transactional
    public void deleteRequiredCase(Long id) {
        LegalCase legalCase = getRequiredCase(id);
        legalCaseRepository.delete(legalCase);
    }

    public Page<LegalCase> searchCases(String keyword, String status, String sort, int page, int size) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        String normalizedStatus = (status == null || status.isBlank()) ? "ALL" : status.trim();
        String normalizedSort = (sort == null || sort.isBlank()) ? "newest" : sort.trim();

        Pageable pageable = PageRequest.of(page, size, buildSort(normalizedSort));

        boolean hasKeyword = !normalizedKeyword.isBlank();
        boolean hasStatus = !"ALL".equalsIgnoreCase(normalizedStatus);

        if (hasKeyword && hasStatus) {
            CaseStatus caseStatus = CaseStatus.valueOf(normalizedStatus.toUpperCase());
            return legalCaseRepository.searchByStatusAndKeyword(caseStatus, normalizedKeyword, pageable);
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

    public String getFriendlyStatus(CaseStatus status) {
        if (status == null) {
            return "Unknown";
        }

        return switch (status) {
            case OPEN -> "Open";
            case IN_PROGRESS -> "In Progress";
            case ON_HOLD -> "On Hold";
            case CLOSED -> "Closed";
        };
    }

    public String getFriendlyType(CaseType type) {
        if (type == null) {
            return "Unknown";
        }

        return switch (type) {
            case CONTRACT -> "Contract";
            case COMPLIANCE -> "Compliance";
            case LITIGATION -> "Litigation";
            case CORPORATE -> "Corporate";
            case OTHER -> "Other";
        };
    }

    public String getStatusBadgeClass(CaseStatus status) {
        if (status == null) {
            return "bg-secondary";
        }

        return switch (status) {
            case OPEN -> "bg-primary";
            case IN_PROGRESS -> "bg-warning text-dark";
            case ON_HOLD -> "bg-secondary";
            case CLOSED -> "bg-success";
        };
    }
}