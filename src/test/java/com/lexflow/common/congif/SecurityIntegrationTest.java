package com.lexflow.common.config;

import com.lexflow.case_.service.LegalCaseService;
import com.lexflow.deadline.service.DeadlineService;
import com.lexflow.document.service.DocumentService;
import com.lexflow.note.service.NoteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({SecurityConfig.class, com.lexflow.common.controller.AuthController.class, com.lexflow.common.controller.ErrorPageController.class})
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LegalCaseService legalCaseService;

    @MockBean
    private DeadlineService deadlineService;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private NoteService noteService;

    @Test
    @DisplayName("Anonymous user is redirected to login page from home")
    void anonymousUser_shouldBeRedirectedToLogin_fromHome() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Anonymous user is redirected to login page from cases")
    void anonymousUser_shouldBeRedirectedToLogin_fromCases() throws Exception {
        mockMvc.perform(get("/cases"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("USER can open regular protected page")
    void user_shouldAccessHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("USER cannot access admin-only new case page")
    void user_shouldBeForbidden_fromNewCasePage() throws Exception {
        mockMvc.perform(get("/cases/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("ADMIN can access new case page")
    void admin_shouldAccessNewCasePage() throws Exception {
        mockMvc.perform(get("/cases/new"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("USER cannot submit admin-only POST action")
    void user_shouldBeForbidden_fromCreateCasePost() throws Exception {
        mockMvc.perform(post("/cases")
                        .with(csrf())
                        .param("title", "Test case")
                        .param("client", "Client")
                        .param("type", "CONTRACT")
                        .param("status", "OPEN"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("ADMIN can submit admin-only POST action")
    void admin_shouldBeAllowed_toCreateCasePost() throws Exception {
        mockMvc.perform(post("/cases")
                        .with(csrf())
                        .param("title", "Test case")
                        .param("client", "Client")
                        .param("type", "CONTRACT")
                        .param("status", "OPEN"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Anonymous user can open custom login page")
    void anonymousUser_shouldAccessLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Authenticated user can logout")
    void authenticatedUser_shouldLogout() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
}