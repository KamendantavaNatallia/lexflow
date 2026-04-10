package com.lexflow.case_.repository;

import com.lexflow.case_.model.LegalCase;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LegalCaseRepository extends JpaRepository<LegalCase, Long> {

    List<LegalCase> findByTitleContainingIgnoreCaseOrClientContainingIgnoreCase(String title, String client, Sort sort);

    List<LegalCase> findByStatus(String status, Sort sort);

    List<LegalCase> findByStatusAndTitleContainingIgnoreCaseOrStatusAndClientContainingIgnoreCase(
            String status1, String title,
            String status2, String client,
            Sort sort
    );
}