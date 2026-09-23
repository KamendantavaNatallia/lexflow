package com.lexflow.deadline.service;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.repository.LegalCaseRepository;
import com.lexflow.common.exception.ResourceNotFoundException;
import com.lexflow.deadline.model.Deadline;
import com.lexflow.deadline.repository.DeadlineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DeadlineService {

    private final DeadlineRepository deadlineRepository;
    private final LegalCaseRepository legalCaseRepository;

    public DeadlineService(DeadlineRepository deadlineRepository,
                           LegalCaseRepository legalCaseRepository) {
        this.deadlineRepository = deadlineRepository;
        this.legalCaseRepository = legalCaseRepository;
    }

    public Deadline addDeadlineToCase(Long caseId, Deadline deadline) {
        LegalCase legalCase = legalCaseRepository.findById(caseId).orElse(null);
        if (legalCase == null) {
            return null;
        }

        deadline.setLegalCase(legalCase);
        deadline.setCompleted(false);

        return deadlineRepository.save(deadline);
    }

    public Deadline saveDeadline(Deadline deadline) {
        return deadlineRepository.save(deadline);
    }

    public List<Deadline> getOverdueDeadlines() {
        return deadlineRepository.findByDueDateBeforeAndCompletedFalse(LocalDate.now());
    }

    public List<Deadline> getUpcomingDeadlines() {
        return deadlineRepository.findByDueDateGreaterThanEqualAndCompletedFalse(LocalDate.now());
    }

    public long getOverdueCount() {
        return deadlineRepository.countByDueDateBeforeAndCompletedFalse(LocalDate.now());
    }

    public long getUpcomingCount() {
        return deadlineRepository.countByDueDateGreaterThanEqualAndCompletedFalse(LocalDate.now());
    }

    public Deadline getDeadlineById(Long id) {
        return deadlineRepository.findById(id).orElse(null);
    }

    public void markCompleted(Long id) {
        Deadline deadline = deadlineRepository.findById(id).orElse(null);
        if (deadline != null) {
            deadline.setCompleted(true);
            deadlineRepository.save(deadline);
        }
    }

    public void deleteDeadline(Long id) {
        Deadline deadline = deadlineRepository.findById(id).orElse(null);
        if (deadline != null) {
            deadlineRepository.delete(deadline);
        }
    }

    // ---------- REST API methods: missing entities throw ResourceNotFoundException (HTTP 404) ----------

    @Transactional(readOnly = true)
    public List<Deadline> getDeadlinesForCase(Long caseId) {
        if (!legalCaseRepository.existsById(caseId)) {
            throw new ResourceNotFoundException("Legal case not found with id: " + caseId);
        }
        return deadlineRepository.findByLegalCaseIdOrderByDueDateAsc(caseId);
    }

    @Transactional
    public Deadline createDeadline(Long caseId, Deadline deadline) {
        LegalCase legalCase = legalCaseRepository.findById(caseId)
                .orElseThrow(() -> new ResourceNotFoundException("Legal case not found with id: " + caseId));

        deadline.setLegalCase(legalCase);
        deadline.setCompleted(false);
        return deadlineRepository.save(deadline);
    }

    public Deadline getRequiredDeadline(Long id) {
        return deadlineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deadline not found with id: " + id));
    }

    /**
     * Updates title, due date and priority. The completed flag and the owning case do not change here.
     */
    @Transactional
    public Deadline updateDeadline(Long id, Deadline changes) {
        Deadline existing = getRequiredDeadline(id);
        existing.setTitle(changes.getTitle());
        existing.setDueDate(changes.getDueDate());
        existing.setPriority(changes.getPriority());
        return existing;
    }

    /**
     * Idempotent: completing an already completed deadline is not an error.
     */
    @Transactional
    public Deadline completeDeadline(Long id) {
        Deadline existing = getRequiredDeadline(id);
        existing.setCompleted(true);
        return existing;
    }

    @Transactional
    public void deleteRequiredDeadline(Long id) {
        Deadline existing = getRequiredDeadline(id);
        deadlineRepository.delete(existing);
    }
}
