package com.lexflow.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test of the REST API with the full Spring context and an in-memory H2 database.
 * Nothing is mocked: controller -> service -> repository -> database, plus the real security config.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    private static final RequestPostProcessor ADMIN = httpBasic("admin", "admin123");
    private static final RequestPostProcessor USER = httpBasic("user", "user123");

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("OpenAPI spec is public and documents the API endpoints")
    void openApiSpec_shouldBePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("LexFlow API"))
                .andExpect(jsonPath("$.paths['/api/v1/cases']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/cases/{caseId}/deadlines']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/notes/{id}']").exists());
    }

    @Test
    @DisplayName("Full lifecycle: create case, add deadline and note, complete deadline, delete case with children")
    void caseLifecycle_shouldWorkEndToEnd() throws Exception {
        // 1. ADMIN creates a case
        String caseJson = mockMvc.perform(post("/api/v1/cases").with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "title": "Lease termination", "client": "Globex", "type": "CONTRACT", "status": "OPEN" }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long caseId = ((Number) JsonPath.read(caseJson, "$.id")).longValue();

        // 2. ADMIN adds a deadline that is already in the past
        String deadlineJson = mockMvc.perform(post("/api/v1/cases/" + caseId + "/deadlines").with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "title": "Send termination notice", "dueDate": "2020-01-31", "priority": "URGENT" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.overdue").value(true))
                .andReturn().getResponse().getContentAsString();
        long deadlineId = ((Number) JsonPath.read(deadlineJson, "$.id")).longValue();

        // 3. ADMIN adds a note
        String noteJson = mockMvc.perform(post("/api/v1/cases/" + caseId + "/notes").with(ADMIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "content": "Landlord agreed to a call on Monday" }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long noteId = ((Number) JsonPath.read(noteJson, "$.id")).longValue();

        // 4. USER can read everything that was created
        mockMvc.perform(get("/api/v1/cases/" + caseId + "/deadlines").with(USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].caseId").value(caseId));

        mockMvc.perform(get("/api/v1/cases/" + caseId + "/notes").with(USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Landlord agreed to a call on Monday"));

        // 5. ADMIN completes the deadline: it is no longer overdue
        mockMvc.perform(patch("/api/v1/deadlines/" + deadlineId + "/complete").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.overdue").value(false));

        // 6. Deleting the case also deletes its deadlines and notes (JPA cascade)
        mockMvc.perform(delete("/api/v1/cases/" + caseId).with(ADMIN))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/cases/" + caseId).with(USER)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/deadlines/" + deadlineId).with(USER)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/notes/" + noteId).with(USER)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Non-numeric id returns 400, not 500")
    void nonNumericId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/cases/abc").with(USER))
                .andExpect(status().isBadRequest());
    }
}
