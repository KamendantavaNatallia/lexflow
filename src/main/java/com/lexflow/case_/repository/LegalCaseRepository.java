package com.lexflow.case_.repository;

import com.lexflow.case_.model.CaseStatus;
import com.lexflow.case_.model.LegalCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegalCaseRepository extends JpaRepository<LegalCase, Long> {

    Page<LegalCase> findByTitleContainingIgnoreCaseOrClientContainingIgnoreCase(
            String title, String client, Pageable pageable
    );

    Page<LegalCase> findByStatus(CaseStatus status, Pageable pageable);

    Page<LegalCase> findByStatusAndTitleContainingIgnoreCaseOrStatusAndClientContainingIgnoreCase(
            CaseStatus status1, String title,
            CaseStatus status2, String client,
            Pageable pageable
    );
}