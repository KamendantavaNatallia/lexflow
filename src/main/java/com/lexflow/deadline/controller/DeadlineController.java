package com.lexflow.deadline.controller;

import com.lexflow.deadline.service.DeadlineService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DeadlineController {

    private final DeadlineService deadlineService;

    public DeadlineController(DeadlineService deadlineService) {
        this.deadlineService = deadlineService;
    }

    @GetMapping("/deadlines/overdue")
    public String overdueDeadlines(Model model) {
        model.addAttribute("deadlines", deadlineService.getOverdueDeadlines());
        model.addAttribute("pageTitle", "Overdue Deadlines");
        model.addAttribute("pageDescription", "Deadlines with due dates before today.");
        return "overdue-deadlines";
    }

    @GetMapping("/deadlines/upcoming")
    public String upcomingDeadlines(Model model) {
        model.addAttribute("deadlines", deadlineService.getUpcomingDeadlines());
        model.addAttribute("pageTitle", "Upcoming Deadlines");
        model.addAttribute("pageDescription", "Deadlines due today or later.");
        return "upcoming-deadlines";
    }
}