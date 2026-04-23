package com.lexflow.case_.controller;

import com.lexflow.case_.model.CaseStatus;
import com.lexflow.case_.model.CaseType;
import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.service.LegalCaseService;
import com.lexflow.document.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CaseController.class)
@AutoConfigureMockMvc(addFilters = false)
class CaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "legalCaseService")
    private LegalCaseService legalCaseService;

    @MockBean
    private DocumentService documentService;

    @Test
    void cases_shouldReturnCasesViewWithPaginationData() throws Exception {
        LegalCase legalCase = new LegalCase();
        legalCase.setTitle("Contract Review");

        Page<LegalCase> casesPage = new PageImpl<>(List.of(legalCase));

        when(legalCaseService.searchCases(null, "ALL", "newest", 0, 5)).thenReturn(casesPage);

        mockMvc.perform(get("/cases"))
                .andExpect(status().isOk())
                .andExpect(view().name("cases"))
                .andExpect(model().attributeExists("casesPage"))
                .andExpect(model().attributeExists("cases"))
                .andExpect(model().attribute("keyword", ""))
                .andExpect(model().attribute("selectedStatus", "ALL"))
                .andExpect(model().attribute("selectedSort", "newest"))
                .andExpect(model().attribute("selectedSize", 5))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1));

        verify(legalCaseService).searchCases(null, "ALL", "newest", 0, 5);
    }

    @Test
    void showNewCaseForm_shouldReturnCaseForm() throws Exception {
        mockMvc.perform(get("/cases/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("case-form"))
                .andExpect(model().attributeExists("legalCase"))
                .andExpect(model().attribute("formMode", "create"));
    }

    @Test
    void addCase_shouldRedirectToCases_whenValid() throws Exception {
        mockMvc.perform(post("/cases")
                        .param("title", "Contract Review")
                        .param("client", "Acme Corp")
                        .param("type", "CONTRACT")
                        .param("status", "OPEN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases"))
                .andExpect(flash().attribute("successMessage", "Case created successfully."));

        verify(legalCaseService).saveCase(any(LegalCase.class));
    }

    @Test
    void addCase_shouldReturnForm_whenValidationFails() throws Exception {
        mockMvc.perform(post("/cases")
                        .param("title", "")
                        .param("client", "")
                        .param("type", "CONTRACT")
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(view().name("case-form"))
                .andExpect(model().attribute("formMode", "create"))
                .andExpect(model().attributeHasFieldErrors("legalCase", "title", "client"));

        verify(legalCaseService, never()).saveCase(any());
    }

    @Test
    void caseDetails_shouldReturnCaseDetails_whenCaseExists() throws Exception {
        LegalCase legalCase = new LegalCase();
        legalCase.setTitle("Contract Review");
        legalCase.setType(CaseType.CONTRACT);
        legalCase.setStatus(CaseStatus.OPEN);

        when(legalCaseService.getCaseById(1L)).thenReturn(legalCase);

        mockMvc.perform(get("/cases/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("case-details"))
                .andExpect(model().attribute("legalCase", legalCase))
                .andExpect(model().attributeExists("documentService"));

        verify(legalCaseService).getCaseById(1L);
    }

    @Test
    void caseDetails_shouldRedirectToCases_whenCaseDoesNotExist() throws Exception {
        when(legalCaseService.getCaseById(999L)).thenReturn(null);

        mockMvc.perform(get("/cases/999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases"));

        verify(legalCaseService).getCaseById(999L);
    }

    @Test
    void showEditCaseForm_shouldReturnCaseForm_whenCaseExists() throws Exception {
        LegalCase legalCase = new LegalCase();
        legalCase.setTitle("Existing Case");

        when(legalCaseService.getCaseById(1L)).thenReturn(legalCase);

        mockMvc.perform(get("/cases/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("case-form"))
                .andExpect(model().attribute("legalCase", legalCase))
                .andExpect(model().attribute("formMode", "edit"));

        verify(legalCaseService).getCaseById(1L);
    }

    @Test
    void showEditCaseForm_shouldRedirectToCases_whenCaseDoesNotExist() throws Exception {
        when(legalCaseService.getCaseById(999L)).thenReturn(null);

        mockMvc.perform(get("/cases/999/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases"));

        verify(legalCaseService).getCaseById(999L);
    }

    @Test
    void updateCase_shouldRedirectToCaseDetails_whenValid() throws Exception {
        LegalCase existingCase = new LegalCase();
        existingCase.setTitle("Old Title");
        existingCase.setClient("Old Client");
        existingCase.setType(CaseType.OTHER);
        existingCase.setStatus(CaseStatus.OPEN);

        when(legalCaseService.getCaseById(1L)).thenReturn(existingCase);

        mockMvc.perform(post("/cases/1/edit")
                        .param("title", "Updated Title")
                        .param("client", "Updated Client")
                        .param("type", "CONTRACT")
                        .param("status", "CLOSED"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases/1"))
                .andExpect(flash().attribute("successMessage", "Case updated successfully."));

        verify(legalCaseService).getCaseById(1L);
        verify(legalCaseService).saveCase(existingCase);
    }

    @Test
    void updateCase_shouldReturnForm_whenValidationFails() throws Exception {
        LegalCase existingCase = new LegalCase();
        existingCase.setTitle("Old Title");
        existingCase.setClient("Old Client");
        existingCase.setType(CaseType.OTHER);
        existingCase.setStatus(CaseStatus.OPEN);

        when(legalCaseService.getCaseById(1L)).thenReturn(existingCase);

        mockMvc.perform(post("/cases/1/edit")
                        .param("title", "")
                        .param("client", "")
                        .param("type", "CONTRACT")
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(view().name("case-form"))
                .andExpect(model().attribute("formMode", "edit"))
                .andExpect(model().attributeHasFieldErrors("legalCase", "title", "client"));

        verify(legalCaseService).getCaseById(1L);
        verify(legalCaseService, never()).saveCase(any());
    }

    @Test
    void updateCase_shouldRedirectToCases_whenCaseDoesNotExist() throws Exception {
        when(legalCaseService.getCaseById(999L)).thenReturn(null);

        mockMvc.perform(post("/cases/999/edit")
                        .param("title", "Updated Title")
                        .param("client", "Updated Client")
                        .param("type", "CONTRACT")
                        .param("status", "OPEN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases"));

        verify(legalCaseService).getCaseById(999L);
        verify(legalCaseService, never()).saveCase(any());
    }

    @Test
    void deleteCase_shouldRedirectToCases() throws Exception {
        mockMvc.perform(post("/cases/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cases"))
                .andExpect(flash().attribute("successMessage", "Case deleted successfully."));

        verify(legalCaseService).deleteCase(1L);
    }
}
