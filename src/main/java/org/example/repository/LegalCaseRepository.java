package org.example.repository;

import org.example.model.CaseStatus;
import org.example.model.LegalCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LegalCaseRepository extends JpaRepository<LegalCase, Long> {
    List<LegalCase> findByStatus(CaseStatus status);
}