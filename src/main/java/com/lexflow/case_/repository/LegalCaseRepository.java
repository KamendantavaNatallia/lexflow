package com.lexflow.case_.repository;

import com.lexflow.case_.model.CaseStatus;
import com.lexflow.case_.model.LegalCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LegalCaseRepository extends JpaRepository<LegalCase, Long> {

    Page<LegalCase> findByTitleContainingIgnoreCaseOrClientContainingIgnoreCase(
            String title, String client, Pageable pageable
    );

    Page<LegalCase> findByStatus(CaseStatus status, Pageable pageable);

    long countByStatus(CaseStatus status);

    @Query("""
            SELECT c
            FROM LegalCase c
            WHERE c.status = :status
              AND (
                    LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(c.client) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<LegalCase> searchByStatusAndKeyword(
            @Param("status") CaseStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}