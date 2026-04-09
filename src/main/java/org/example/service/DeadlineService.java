package org.example.service;

import org.example.model.Deadline;
import org.example.model.LegalCase;
import org.example.repository.DeadlineRepository;
import org.example.repository.LegalCaseRepository;
import org.springframework.stereotype.Service;

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
        legalCase.getDeadlines().add(deadline);
        legalCaseRepository.save(legalCase);

        return deadline;
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
        return getOverdueDeadlines().size();
    }

    public long getUpcomingCount() {
        return getUpcomingDeadlines().size();
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
}