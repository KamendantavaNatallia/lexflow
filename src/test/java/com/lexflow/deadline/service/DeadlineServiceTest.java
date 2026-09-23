package com.lexflow.deadline.service;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.repository.LegalCaseRepository;
import com.lexflow.common.exception.ResourceNotFoundException;
import com.lexflow.deadline.model.Deadline;
import com.lexflow.deadline.model.DeadlinePriority;
import com.lexflow.deadline.repository.DeadlineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeadlineServiceTest {

    @Mock
    private DeadlineRepository deadlineRepository;

    @Mock
    private LegalCaseRepository legalCaseRepository;

    @InjectMocks
    private DeadlineService deadlineService;

    @Test
    void addDeadlineToCase_shouldReturnNull_whenCaseDoesNotExist() {
        Deadline deadline = new Deadline();

        when(legalCaseRepository.findById(1L)).thenReturn(Optional.empty());

        Deadline result = deadlineService.addDeadlineToCase(1L, deadline);

        assertNull(result);
        verify(legalCaseRepository).findById(1L);
        verify(deadlineRepository, never()).save(any());
        verify(legalCaseRepository, never()).save(any());
    }

    @Test
    void addDeadlineToCase_shouldAttachDeadlineToCaseAndSave() {
        LegalCase legalCase = new LegalCase();

        Deadline deadline = new Deadline();
        deadline.setTitle("Submit filing");
        deadline.setDueDate(LocalDate.now().plusDays(3));

        when(legalCaseRepository.findById(1L)).thenReturn(Optional.of(legalCase));
        when(deadlineRepository.save(deadline)).thenReturn(deadline);

        Deadline result = deadlineService.addDeadlineToCase(1L, deadline);

        assertNotNull(result);
        assertEquals(deadline, result);
        assertEquals(legalCase, deadline.getLegalCase());
        assertFalse(deadline.isCompleted());

        verify(legalCaseRepository).findById(1L);
        verify(deadlineRepository).save(deadline);
        verify(legalCaseRepository, never()).save(any());
    }

    @Test
    void saveDeadline_shouldCallRepositorySave() {
        Deadline deadline = new Deadline();
        deadline.setTitle("Court hearing");

        when(deadlineRepository.save(deadline)).thenReturn(deadline);

        Deadline result = deadlineService.saveDeadline(deadline);

        assertEquals(deadline, result);
        verify(deadlineRepository).save(deadline);
    }

    @Test
    void getOverdueDeadlines_shouldReturnRepositoryResult() {
        Deadline deadline = new Deadline();
        deadline.setTitle("Overdue item");

        List<Deadline> overdue = List.of(deadline);

        when(deadlineRepository.findByDueDateBeforeAndCompletedFalse(LocalDate.now()))
                .thenReturn(overdue);

        List<Deadline> result = deadlineService.getOverdueDeadlines();

        assertEquals(1, result.size());
        assertEquals("Overdue item", result.get(0).getTitle());
        verify(deadlineRepository).findByDueDateBeforeAndCompletedFalse(LocalDate.now());
    }

    @Test
    void getUpcomingDeadlines_shouldReturnRepositoryResult() {
        Deadline deadline = new Deadline();
        deadline.setTitle("Upcoming item");

        List<Deadline> upcoming = List.of(deadline);

        when(deadlineRepository.findByDueDateGreaterThanEqualAndCompletedFalse(LocalDate.now()))
                .thenReturn(upcoming);

        List<Deadline> result = deadlineService.getUpcomingDeadlines();

        assertEquals(1, result.size());
        assertEquals("Upcoming item", result.get(0).getTitle());
        verify(deadlineRepository).findByDueDateGreaterThanEqualAndCompletedFalse(LocalDate.now());
    }

    @Test
    void getOverdueCount_shouldReturnRepositoryCount() {
        when(deadlineRepository.countByDueDateBeforeAndCompletedFalse(LocalDate.now()))
                .thenReturn(2L);

        long result = deadlineService.getOverdueCount();

        assertEquals(2L, result);
        verify(deadlineRepository).countByDueDateBeforeAndCompletedFalse(LocalDate.now());
    }

    @Test
    void getUpcomingCount_shouldReturnRepositoryCount() {
        when(deadlineRepository.countByDueDateGreaterThanEqualAndCompletedFalse(LocalDate.now()))
                .thenReturn(3L);

        long result = deadlineService.getUpcomingCount();

        assertEquals(3L, result);
        verify(deadlineRepository).countByDueDateGreaterThanEqualAndCompletedFalse(LocalDate.now());
    }

    @Test
    void getDeadlineById_shouldReturnDeadline_whenFound() {
        Deadline deadline = new Deadline();
        deadline.setTitle("Review contract");

        when(deadlineRepository.findById(1L)).thenReturn(Optional.of(deadline));

        Deadline result = deadlineService.getDeadlineById(1L);

        assertNotNull(result);
        assertEquals("Review contract", result.getTitle());
        verify(deadlineRepository).findById(1L);
    }

    @Test
    void getDeadlineById_shouldReturnNull_whenNotFound() {
        when(deadlineRepository.findById(999L)).thenReturn(Optional.empty());

        Deadline result = deadlineService.getDeadlineById(999L);

        assertNull(result);
        verify(deadlineRepository).findById(999L);
    }

    @Test
    void markCompleted_shouldSetCompletedTrueAndSave_whenDeadlineExists() {
        Deadline deadline = new Deadline();
        deadline.setCompleted(false);

        when(deadlineRepository.findById(1L)).thenReturn(Optional.of(deadline));

        deadlineService.markCompleted(1L);

        assertTrue(deadline.isCompleted());
        verify(deadlineRepository).findById(1L);
        verify(deadlineRepository).save(deadline);
    }

    @Test
    void markCompleted_shouldDoNothing_whenDeadlineDoesNotExist() {
        when(deadlineRepository.findById(999L)).thenReturn(Optional.empty());

        deadlineService.markCompleted(999L);

        verify(deadlineRepository).findById(999L);
        verify(deadlineRepository, never()).save(any());
    }

    @Test
    void deleteDeadline_shouldDeleteDeadline_whenDeadlineExists() {
        Deadline deadline = new Deadline();

        when(deadlineRepository.findById(1L)).thenReturn(Optional.of(deadline));

        deadlineService.deleteDeadline(1L);

        verify(deadlineRepository).findById(1L);
        verify(deadlineRepository).delete(deadline);
    }

    @Test
    void deleteDeadline_shouldDoNothing_whenDeadlineDoesNotExist() {
        when(deadlineRepository.findById(999L)).thenReturn(Optional.empty());

        deadlineService.deleteDeadline(999L);

        verify(deadlineRepository).findById(999L);
        verify(deadlineRepository, never()).delete(any());
    }

    // ---------- REST API methods ----------

    @Test
    void getDeadlinesForCase_shouldThrowNotFound_whenCaseDoesNotExist() {
        when(legalCaseRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> deadlineService.getDeadlinesForCase(99L));
        verify(deadlineRepository, never()).findByLegalCaseIdOrderByDueDateAsc(any());
    }

    @Test
    void getDeadlinesForCase_shouldReturnDeadlinesOfCase() {
        Deadline deadline = new Deadline("File appeal", LocalDate.now().plusDays(10), DeadlinePriority.HIGH);
        when(legalCaseRepository.existsById(1L)).thenReturn(true);
        when(deadlineRepository.findByLegalCaseIdOrderByDueDateAsc(1L)).thenReturn(List.of(deadline));

        List<Deadline> result = deadlineService.getDeadlinesForCase(1L);

        assertEquals(1, result.size());
        assertEquals("File appeal", result.get(0).getTitle());
    }

    @Test
    void createDeadline_shouldAttachToCase_andStartNotCompleted() {
        LegalCase legalCase = new LegalCase();
        Deadline deadline = new Deadline("File appeal", LocalDate.now().plusDays(10), DeadlinePriority.HIGH);
        deadline.setCompleted(true);
        when(legalCaseRepository.findById(1L)).thenReturn(Optional.of(legalCase));
        when(deadlineRepository.save(deadline)).thenReturn(deadline);

        Deadline result = deadlineService.createDeadline(1L, deadline);

        assertSame(legalCase, result.getLegalCase());
        assertFalse(result.isCompleted());
        verify(deadlineRepository).save(deadline);
    }

    @Test
    void createDeadline_shouldThrowNotFound_whenCaseDoesNotExist() {
        when(legalCaseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> deadlineService.createDeadline(99L, new Deadline()));
        verify(deadlineRepository, never()).save(any());
    }

    @Test
    void getRequiredDeadline_shouldThrowNotFound_whenDeadlineDoesNotExist() {
        when(deadlineRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> deadlineService.getRequiredDeadline(99L));

        assertEquals("Deadline not found with id: 99", exception.getMessage());
    }

    @Test
    void updateDeadline_shouldChangeTitleDateAndPriority_butKeepCompletedFlag() {
        Deadline existing = new Deadline("Old", LocalDate.of(2026, 1, 1), DeadlinePriority.LOW);
        existing.setCompleted(true);
        Deadline changes = new Deadline("New", LocalDate.of(2026, 12, 31), DeadlinePriority.URGENT);
        when(deadlineRepository.findById(1L)).thenReturn(Optional.of(existing));

        Deadline result = deadlineService.updateDeadline(1L, changes);

        assertEquals("New", result.getTitle());
        assertEquals(LocalDate.of(2026, 12, 31), result.getDueDate());
        assertEquals(DeadlinePriority.URGENT, result.getPriority());
        assertTrue(result.isCompleted());
    }

    @Test
    void completeDeadline_shouldSetCompleted() {
        Deadline existing = new Deadline("File appeal", LocalDate.now(), DeadlinePriority.HIGH);
        when(deadlineRepository.findById(1L)).thenReturn(Optional.of(existing));

        Deadline result = deadlineService.completeDeadline(1L);

        assertTrue(result.isCompleted());
    }

    @Test
    void deleteRequiredDeadline_shouldThrowNotFound_andNotDelete_whenDeadlineDoesNotExist() {
        when(deadlineRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> deadlineService.deleteRequiredDeadline(99L));
        verify(deadlineRepository, never()).delete(any());
    }
}
