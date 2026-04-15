package com.lexflow.deadline.service;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.repository.LegalCaseRepository;
import com.lexflow.deadline.model.Deadline;
import com.lexflow.deadline.repository.DeadlineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
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
        verify(legalCaseRepository, never()).save(any());
    }

    @Test
    void addDeadlineToCase_shouldAttachDeadlineToCaseAndSave() {
        LegalCase legalCase = new LegalCase();
        legalCase.setDeadlines(new ArrayList<>());

        Deadline deadline = new Deadline();
        deadline.setTitle("Submit filing");
        deadline.setDueDate(LocalDate.now().plusDays(3));

        when(legalCaseRepository.findById(1L)).thenReturn(Optional.of(legalCase));

        Deadline result = deadlineService.addDeadlineToCase(1L, deadline);

        assertNotNull(result);
        assertEquals(legalCase, deadline.getLegalCase());
        assertFalse(deadline.isCompleted());
        assertEquals(1, legalCase.getDeadlines().size());
        assertEquals(deadline, legalCase.getDeadlines().get(0));

        verify(legalCaseRepository).findById(1L);
        verify(legalCaseRepository).save(legalCase);
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
    void getOverdueCount_shouldReturnSizeOfOverdueDeadlines() {
        List<Deadline> overdue = List.of(new Deadline(), new Deadline());

        when(deadlineRepository.findByDueDateBeforeAndCompletedFalse(LocalDate.now()))
                .thenReturn(overdue);

        long result = deadlineService.getOverdueCount();

        assertEquals(2L, result);
        verify(deadlineRepository).findByDueDateBeforeAndCompletedFalse(LocalDate.now());
    }

    @Test
    void getUpcomingCount_shouldReturnSizeOfUpcomingDeadlines() {
        List<Deadline> upcoming = List.of(new Deadline(), new Deadline(), new Deadline());

        when(deadlineRepository.findByDueDateGreaterThanEqualAndCompletedFalse(LocalDate.now()))
                .thenReturn(upcoming);

        long result = deadlineService.getUpcomingCount();

        assertEquals(3L, result);
        verify(deadlineRepository).findByDueDateGreaterThanEqualAndCompletedFalse(LocalDate.now());
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
}