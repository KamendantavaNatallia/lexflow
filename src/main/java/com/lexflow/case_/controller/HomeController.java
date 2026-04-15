package com.lexflow.case_.controller;

import com.lexflow.case_.service.LegalCaseService;
import com.lexflow.deadline.service.DeadlineService;
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
        model.addAttribute("totalCases", legalCaseService.getTotalCasesCount());
        model.addAttribute("openCases", legalCaseService.getOpenCasesCount());
        model.addAttribute("overdueDeadlines", deadlineService.getOverdueDeadlines().size());
        model.addAttribute("upcomingDeadlines", deadlineService.getUpcomingDeadlines().size());
        model.addAttribute("recentCases", legalCaseService.getRecentCases(5));
        model.addAttribute("legalCaseService", legalCaseService);

        return "index";
    }
}