package com.lexflow.case_.repository;

import com.lexflow.case_.model.CaseStatus;
import com.lexflow.case_.model.LegalCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LegalCaseRepository extends JpaRepository<LegalCase, Long> {
    List<LegalCase> findByStatus(CaseStatus status);
}