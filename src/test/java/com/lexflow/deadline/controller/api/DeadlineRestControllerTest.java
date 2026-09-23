package com.lexflow.deadline.controller.api;

import com.lexflow.case_.model.CaseStatus;
import com.lexflow.case_.model.CaseType;
import com.lexflow.case_.model.LegalCase;
import com.lexflow.common.config.SecurityConfig;
import com.lexflow.common.exception.ResourceNotFoundException;
import com.lexflow.deadline.mapper.DeadlineMapper;
import com.lexflow.deadline.model.Deadline;
import com.lexflow.deadline.model.DeadlinePriority;
import com.lexflow.deadline.service.DeadlineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DeadlineRestController.class)
@Import({SecurityConfig.class, DeadlineMapper.class})
class DeadlineRestControllerTest {

    private static final String VALID_BODY = """
            {
              "title": "File statement of claim",
              "dueDate": "2030-03-15",
              "priority": "HIGH"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeadlineService deadlineService;

    private static Deadline deadline(Long id, LocalDate dueDate, boolean completed) {
        LegalCase legalCase = new LegalCase("Supply dispute", "Acme LLC", CaseType.LITIGATION, CaseStatus.OPEN);
        ReflectionTestUtils.setField(legalCase, "id", 1L);

        Deadline deadline = new Deadline("File statement of claim", dueDate, DeadlinePriority.HIGH);
        ReflectionTestUtils.setField(deadline, "id", id);
        deadline.setLegalCase(legalCase);
        deadline.setCompleted(completed);
        return deadline;
    }

    @Test
    @DisplayName("USER lists deadlines of a case; response includes caseId and computed overdue flag")
    void user_shouldListDeadlinesOfCase() throws Exception {
        when(deadlineService.getDeadlinesForCase(1L)).thenReturn(List.of(
                deadline(10L, LocalDate.of(2000, 1, 1), false),
                deadline(11L, LocalDate.of(2100, 1, 1), false)
        ));

        mockMvc.perform(get("/api/v1/cases/1/deadlines").with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].caseId").value(1))
                .andExpect(jsonPath("$[0].dueDate").value("2000-01-01"))
                .andExpect(jsonPath("$[0].overdue").value(true))
                .andExpect(jsonPath("$[1].overdue").value(false));
    }

    @Test
    @DisplayName("Listing deadlines of a missing case returns 404")
    void listForMissingCase_shouldReturnNotFound() throws Exception {
        when(deadlineService.getDeadlinesForCase(99L))
                .thenThrow(new ResourceNotFoundException("Legal case not found with id: 99"));

        mockMvc.perform(get("/api/v1/cases/99/deadlines").with(httpBasic("user", "user123")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Legal case not found with id: 99"));
    }

    @Test
    @DisplayName("ADMIN creates a deadline: 201 with Location /api/v1/deadlines/{id}")
    void admin_shouldCreateDeadline() throws Exception {
        when(deadlineService.createDeadline(eq(1L), any(Deadline.class)))
                .thenReturn(deadline(12L, LocalDate.of(2030, 3, 15), false));

        mockMvc.perform(post("/api/v1/cases/1/deadlines")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/deadlines/12")))
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    @DisplayName("USER cannot create a deadline")
    void user_shouldBeForbidden_fromCreate() throws Exception {
        mockMvc.perform(post("/api/v1/cases/1/deadlines")
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());

        verify(deadlineService, never()).createDeadline(any(), any());
    }

    @Test
    @DisplayName("Missing due date and priority return 400 with field errors")
    void createWithInvalidBody_shouldReturnValidationErrors() throws Exception {
        mockMvc.perform(post("/api/v1/cases/1/deadlines")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "title": "File statement of claim" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.dueDate").value("Due date is required"))
                .andExpect(jsonPath("$.errors.priority").value("Priority is required"));

        verify(deadlineService, never()).createDeadline(any(), any());
    }

    @Test
    @DisplayName("Badly formatted date returns 400")
    void createWithBadDate_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/cases/1/deadlines")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("2030-03-15", "15/03/2030")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Missing deadline returns 404")
    void getMissingDeadline_shouldReturnNotFound() throws Exception {
        when(deadlineService.getRequiredDeadline(99L))
                .thenThrow(new ResourceNotFoundException("Deadline not found with id: 99"));

        mockMvc.perform(get("/api/v1/deadlines/99").with(httpBasic("user", "user123")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ADMIN updates a deadline")
    void admin_shouldUpdateDeadline() throws Exception {
        when(deadlineService.updateDeadline(eq(10L), any(Deadline.class)))
                .thenReturn(deadline(10L, LocalDate.of(2030, 3, 15), false));

        mockMvc.perform(put("/api/v1/deadlines/10")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dueDate").value("2030-03-15"));
    }

    @Test
    @DisplayName("ADMIN completes a deadline with PATCH")
    void admin_shouldCompleteDeadline() throws Exception {
        when(deadlineService.completeDeadline(10L)).thenReturn(deadline(10L, LocalDate.of(2000, 1, 1), true));

        mockMvc.perform(patch("/api/v1/deadlines/10/complete").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.overdue").value(false));
    }

    @Test
    @DisplayName("USER cannot complete a deadline")
    void user_shouldBeForbidden_fromComplete() throws Exception {
        mockMvc.perform(patch("/api/v1/deadlines/10/complete").with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden());

        verify(deadlineService, never()).completeDeadline(any());
    }

    @Test
    @DisplayName("Overdue endpoint is not confused with /deadlines/{id}")
    void user_shouldGetOverdueDeadlines() throws Exception {
        when(deadlineService.getOverdueDeadlines()).thenReturn(List.of(deadline(10L, LocalDate.of(2000, 1, 1), false)));

        mockMvc.perform(get("/api/v1/deadlines/overdue").with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].overdue").value(true));
    }

    @Test
    @DisplayName("ADMIN deletes a deadline: 204; missing one: 404")
    void admin_shouldDeleteDeadline() throws Exception {
        mockMvc.perform(delete("/api/v1/deadlines/10").with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());
        verify(deadlineService).deleteRequiredDeadline(10L);

        doThrow(new ResourceNotFoundException("Deadline not found with id: 99"))
                .when(deadlineService).deleteRequiredDeadline(99L);
        mockMvc.perform(delete("/api/v1/deadlines/99").with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound());
    }
}
