package com.lexflow.case_.controller.api;

import com.lexflow.case_.mapper.LegalCaseMapper;
import com.lexflow.case_.model.CaseStatus;
import com.lexflow.case_.model.CaseType;
import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.service.LegalCaseService;
import com.lexflow.common.config.SecurityConfig;
import com.lexflow.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests for the cases REST API.
 * The service is mocked; the real SecurityConfig is loaded, and requests authenticate
 * with HTTP Basic against its in-memory users (admin/admin123, user/user123).
 */
@WebMvcTest(controllers = LegalCaseRestController.class)
@Import({SecurityConfig.class, LegalCaseMapper.class})
class LegalCaseRestControllerTest {

    private static final String BASE_URL = "/api/v1/cases";

    private static final String VALID_BODY = """
            {
              "title": "Supply contract review",
              "client": "Acme LLC",
              "type": "CONTRACT",
              "status": "OPEN"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LegalCaseService legalCaseService;

    private static LegalCase caseWithId(Long id) {
        LegalCase legalCase = new LegalCase("Supply contract review", "Acme LLC", CaseType.CONTRACT, CaseStatus.OPEN);
        ReflectionTestUtils.setField(legalCase, "id", id);
        return legalCase;
    }

    // ---------- authentication and authorization ----------

    @Test
    @DisplayName("Anonymous request gets 401, not a redirect to the login page")
    void anonymous_shouldGetUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Wrong password gets 401")
    void wrongPassword_shouldGetUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL).with(httpBasic("admin", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("USER cannot create a case")
    void user_shouldBeForbidden_fromCreate() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .with(httpBasic("user", "user123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());

        verify(legalCaseService, never()).createCase(any());
    }

    @Test
    @DisplayName("USER cannot delete a case")
    void user_shouldBeForbidden_fromDelete() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1").with(httpBasic("user", "user123")))
                .andExpect(status().isForbidden());

        verify(legalCaseService, never()).deleteRequiredCase(any());
    }

    // ---------- read ----------

    @Test
    @DisplayName("USER can list cases")
    void user_shouldListCases() throws Exception {
        when(legalCaseService.searchCases(any(), eq("ALL"), eq("newest"), eq(0), eq(5)))
                .thenReturn(new PageImpl<>(List.of(caseWithId(1L))));

        mockMvc.perform(get(BASE_URL).with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Supply contract review"));
    }

    @Test
    @DisplayName("USER can get one case by id")
    void user_shouldGetCaseById() throws Exception {
        when(legalCaseService.getRequiredCase(1L)).thenReturn(caseWithId(1L));

        mockMvc.perform(get(BASE_URL + "/1").with(httpBasic("user", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.client").value("Acme LLC"))
                .andExpect(jsonPath("$.type").value("CONTRACT"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("Missing case returns 404 problem detail")
    void getMissingCase_shouldReturnNotFound() throws Exception {
        when(legalCaseService.getRequiredCase(99L))
                .thenThrow(new ResourceNotFoundException("Legal case not found with id: 99"));

        mockMvc.perform(get(BASE_URL + "/99").with(httpBasic("user", "user123")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.detail").value("Legal case not found with id: 99"));
    }

    @Test
    @DisplayName("Unknown status filter returns 400")
    void unknownStatusFilter_shouldReturnBadRequest() throws Exception {
        when(legalCaseService.searchCases(any(), eq("NOPE"), any(), eq(0), eq(5)))
                .thenThrow(new IllegalArgumentException("No enum constant NOPE"));

        mockMvc.perform(get(BASE_URL).param("status", "NOPE").with(httpBasic("user", "user123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ---------- create ----------

    @Test
    @DisplayName("ADMIN creates a case: 201 with Location header, no CSRF token needed")
    void admin_shouldCreateCase() throws Exception {
        when(legalCaseService.createCase(any(LegalCase.class))).thenReturn(caseWithId(7L));

        mockMvc.perform(post(BASE_URL)
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/cases/7")))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.title").value("Supply contract review"));
    }

    @Test
    @DisplayName("Blank title and missing type return 400 with field errors")
    void createWithInvalidBody_shouldReturnValidationErrors() throws Exception {
        String invalidBody = """
                {
                  "title": "   ",
                  "client": "Acme LLC",
                  "status": "OPEN"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").value("Case title is required"))
                .andExpect(jsonPath("$.errors.type").value("Case type is required"));

        verify(legalCaseService, never()).createCase(any());
    }

    @Test
    @DisplayName("Unknown enum value in body returns 400")
    void createWithUnknownType_shouldReturnBadRequest() throws Exception {
        String body = VALID_BODY.replace("CONTRACT", "NOT_A_TYPE");

        mockMvc.perform(post(BASE_URL)
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(legalCaseService, never()).createCase(any());
    }

    // ---------- update ----------

    @Test
    @DisplayName("ADMIN updates a case")
    void admin_shouldUpdateCase() throws Exception {
        LegalCase updated = caseWithId(1L);
        updated.setStatus(CaseStatus.CLOSED);
        when(legalCaseService.updateCase(eq(1L), any(LegalCase.class))).thenReturn(updated);

        mockMvc.perform(put(BASE_URL + "/1")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("OPEN", "CLOSED")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    @DisplayName("Updating a missing case returns 404")
    void updateMissingCase_shouldReturnNotFound() throws Exception {
        when(legalCaseService.updateCase(eq(99L), any(LegalCase.class)))
                .thenThrow(new ResourceNotFoundException("Legal case not found with id: 99"));

        mockMvc.perform(put(BASE_URL + "/99")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound());
    }

    // ---------- delete ----------

    @Test
    @DisplayName("ADMIN deletes a case: 204 No Content")
    void admin_shouldDeleteCase() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1").with(httpBasic("admin", "admin123")))
                .andExpect(status().isNoContent());

        verify(legalCaseService).deleteRequiredCase(1L);
    }

    @Test
    @DisplayName("Deleting a missing case returns 404")
    void deleteMissingCase_shouldReturnNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Legal case not found with id: 99"))
                .when(legalCaseService).deleteRequiredCase(99L);

        mockMvc.perform(delete(BASE_URL + "/99").with(httpBasic("admin", "admin123")))
                .andExpect(status().isNotFound());
    }
}
