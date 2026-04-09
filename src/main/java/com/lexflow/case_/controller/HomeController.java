package com.lexflow.case_.controller;

import com.lexflow.deadline.service.DeadlineService;
import com.lexflow.case_.service.LegalCaseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final LegalCaseService legalCaseService;
    private final DeadlineService deadlineService;

    public HomeController(LegalCaseService legalCaseService,
                          DeadlineService deadlineService) {
        this.legalCaseService = legalCaseService;
        this.deadlineService = deadlineService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("appName", "LexFlow");
        model.addAttribute("message", "Your Legal Deadline Tracker is running successfully.");
        model.addAttribute("totalCases", legalCaseService.getTotalCasesCount());
        model.addAttribute("openCases", legalCaseService.getOpenCasesCount());
        model.addAttribute("overdueDeadlines", deadlineService.getOverdueCount());
        model.addAttribute("upcomingDeadlines", deadlineService.getUpcomingCount());

        return "index";
    }
}