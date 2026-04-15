package com.lexflow.case_.controller;

import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.service.LegalCaseService;
import com.lexflow.deadline.model.Deadline;
import com.lexflow.deadline.service.DeadlineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LegalCaseService legalCaseService;

    @MockBean
    private DeadlineService deadlineService;

    @Test
    void home_shouldReturnIndexViewWithDashboardData() throws Exception {
        LegalCase legalCase1 = new LegalCase();
        legalCase1.setTitle("Contract Review");

        LegalCase legalCase2 = new LegalCase();
        legalCase2.setTitle("Compliance Audit");

        Deadline overdueDeadline = new Deadline();
        Deadline upcomingDeadline1 = new Deadline();
        Deadline upcomingDeadline2 = new Deadline();

        when(legalCaseService.getTotalCasesCount()).thenReturn(10L);
        when(legalCaseService.getOpenCasesCount()).thenReturn(4L);
        when(deadlineService.getOverdueDeadlines()).thenReturn(List.of(overdueDeadline));
        when(deadlineService.getUpcomingDeadlines()).thenReturn(List.of(upcomingDeadline1, upcomingDeadline2));
        when(legalCaseService.getRecentCases(5)).thenReturn(List.of(legalCase1, legalCase2));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("totalCases", 10L))
                .andExpect(model().attribute("openCases", 4L))
                .andExpect(model().attribute("overdueDeadlines", 1))
                .andExpect(model().attribute("upcomingDeadlines", 2))
                .andExpect(model().attribute("recentCases", List.of(legalCase1, legalCase2)))
                .andExpect(model().attributeExists("legalCaseService"));
    }
}