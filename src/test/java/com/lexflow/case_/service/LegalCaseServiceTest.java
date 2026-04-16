package com.lexflow.case_.service;

import com.lexflow.case_.model.CaseStatus;
import com.lexflow.case_.model.CaseType;
import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.repository.LegalCaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LegalCaseServiceTest {

    @Mock
    private LegalCaseRepository legalCaseRepository;

    @InjectMocks
    private LegalCaseService legalCaseService;

    @Test
    void getAllCases_shouldReturnAllCases() {
        LegalCase legalCase = new LegalCase();
        legalCase.setTitle("Contract Review");

        when(legalCaseRepository.findAll()).thenReturn(List.of(legalCase));

        List<LegalCase> result = legalCaseService.getAllCases();

        assertEquals(1, result.size());
        assertEquals("Contract Review", result.get(0).getTitle());
        verify(legalCaseRepository).findAll();
    }

    @Test
    void getCaseById_shouldReturnCase_whenCaseExists() {
        LegalCase legalCase = new LegalCase();
        legalCase.setTitle("Contract Review");
        legalCase.setClient("Acme Corp");
        legalCase.setType(CaseType.CONTRACT);
        legalCase.setStatus(CaseStatus.OPEN);

        when(legalCaseRepository.findById(1L)).thenReturn(Optional.of(legalCase));

        LegalCase result = legalCaseService.getCaseById(1L);

        assertNotNull(result);
        assertEquals("Contract Review", result.getTitle());
        assertEquals("Acme Corp", result.getClient());
        assertEquals(CaseType.CONTRACT, result.getType());
        assertEquals(CaseStatus.OPEN, result.getStatus());
        verify(legalCaseRepository).findById(1L);
    }

    @Test
    void getCaseById_shouldReturnNull_whenCaseDoesNotExist() {
        when(legalCaseRepository.findById(999L)).thenReturn(Optional.empty());

        LegalCase result = legalCaseService.getCaseById(999L);

        assertNull(result);
        verify(legalCaseRepository).findById(999L);
    }

    @Test
    void saveCase_shouldCallRepositorySave() {
        LegalCase legalCase = new LegalCase();
        legalCase.setTitle("New Case");

        legalCaseService.saveCase(legalCase);

        verify(legalCaseRepository).save(legalCase);
    }

    @Test
    void deleteCase_shouldCallRepositoryDeleteById() {
        legalCaseService.deleteCase(1L);

        verify(legalCaseRepository).deleteById(1L);
    }

    @Test
    void getTotalCasesCount_shouldReturnRepositoryCount() {
        when(legalCaseRepository.count()).thenReturn(5L);

        long result = legalCaseService.getTotalCasesCount();

        assertEquals(5L, result);
        verify(legalCaseRepository).count();
    }

    @Test
    void getOpenCasesCount_shouldReturnCountByStatus() {
        when(legalCaseRepository.countByStatus(CaseStatus.OPEN)).thenReturn(2L);

        long result = legalCaseService.getOpenCasesCount();

        assertEquals(2L, result);
        verify(legalCaseRepository).countByStatus(CaseStatus.OPEN);
    }

    @Test
    void getRecentCases_shouldReturnLimitedNumberOfCases() {
        LegalCase case1 = new LegalCase();
        case1.setTitle("Case 3");

        LegalCase case2 = new LegalCase();
        case2.setTitle("Case 2");

        Page<LegalCase> page = new PageImpl<>(List.of(case1, case2));

        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "id"));
        when(legalCaseRepository.findAll(pageable)).thenReturn(page);

        List<LegalCase> result = legalCaseService.getRecentCases(2);

        assertEquals(2, result.size());
        assertEquals("Case 3", result.get(0).getTitle());
        assertEquals("Case 2", result.get(1).getTitle());
        verify(legalCaseRepository).findAll(pageable);
    }

    @Test
    void searchCases_shouldReturnAllCases_whenNoFiltersProvided() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));
        Page<LegalCase> page = new PageImpl<>(List.of(new LegalCase()));

        when(legalCaseRepository.findAll(pageable)).thenReturn(page);

        Page<LegalCase> result = legalCaseService.searchCases("", "ALL", "newest", 0, 5);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(legalCaseRepository).findAll(pageable);
    }

    @Test
    void searchCases_shouldSearchByKeyword_whenKeywordProvided() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));
        Page<LegalCase> page = new PageImpl<>(List.of(new LegalCase()));

        when(legalCaseRepository.findByTitleContainingIgnoreCaseOrClientContainingIgnoreCase(
                "Acme", "Acme", pageable
        )).thenReturn(page);

        Page<LegalCase> result = legalCaseService.searchCases("Acme", "ALL", "newest", 0, 5);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(legalCaseRepository)
                .findByTitleContainingIgnoreCaseOrClientContainingIgnoreCase("Acme", "Acme", pageable);
    }

    @Test
    void searchCases_shouldFilterByStatus_whenStatusProvided() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));
        Page<LegalCase> page = new PageImpl<>(List.of(new LegalCase()));

        when(legalCaseRepository.findByStatus(CaseStatus.OPEN, pageable)).thenReturn(page);

        Page<LegalCase> result = legalCaseService.searchCases("", "OPEN", "newest", 0, 5);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(legalCaseRepository).findByStatus(CaseStatus.OPEN, pageable);
    }

    @Test
    void searchCases_shouldFilterByKeywordAndStatus_whenBothProvided() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "id"));
        Page<LegalCase> page = new PageImpl<>(List.of(new LegalCase()));

        when(legalCaseRepository.searchByStatusAndKeyword(
                CaseStatus.OPEN,
                "Acme",
                pageable
        )).thenReturn(page);

        Page<LegalCase> result = legalCaseService.searchCases("Acme", "OPEN", "newest", 0, 5);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(legalCaseRepository).searchByStatusAndKeyword(CaseStatus.OPEN, "Acme", pageable);
    }

    @Test
    void getFriendlyStatus_shouldReturnReadableLabel() {
        assertEquals("Open", legalCaseService.getFriendlyStatus(CaseStatus.OPEN));
        assertEquals("In Progress", legalCaseService.getFriendlyStatus(CaseStatus.IN_PROGRESS));
        assertEquals("On Hold", legalCaseService.getFriendlyStatus(CaseStatus.ON_HOLD));
        assertEquals("Closed", legalCaseService.getFriendlyStatus(CaseStatus.CLOSED));
        assertEquals("Unknown", legalCaseService.getFriendlyStatus(null));
    }

    @Test
    void getFriendlyType_shouldReturnReadableLabel() {
        assertEquals("Contract", legalCaseService.getFriendlyType(CaseType.CONTRACT));
        assertEquals("Compliance", legalCaseService.getFriendlyType(CaseType.COMPLIANCE));
        assertEquals("Litigation", legalCaseService.getFriendlyType(CaseType.LITIGATION));
        assertEquals("Corporate", legalCaseService.getFriendlyType(CaseType.CORPORATE));
        assertEquals("Other", legalCaseService.getFriendlyType(CaseType.OTHER));
        assertEquals("Unknown", legalCaseService.getFriendlyType(null));
    }

    @Test
    void getStatusBadgeClass_shouldReturnBootstrapClass() {
        assertEquals("bg-primary", legalCaseService.getStatusBadgeClass(CaseStatus.OPEN));
        assertEquals("bg-warning text-dark", legalCaseService.getStatusBadgeClass(CaseStatus.IN_PROGRESS));
        assertEquals("bg-secondary", legalCaseService.getStatusBadgeClass(CaseStatus.ON_HOLD));
        assertEquals("bg-success", legalCaseService.getStatusBadgeClass(CaseStatus.CLOSED));
        assertEquals("bg-secondary", legalCaseService.getStatusBadgeClass(null));
    }
}