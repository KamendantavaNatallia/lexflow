package com.lexflow.deadline.model;

import com.lexflow.case_.model.LegalCase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "deadlines")
public class Deadline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Deadline title is required")
    private String title;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    private DeadlinePriority priority;

    private boolean completed = false;

    @ManyToOne
    @JoinColumn(name = "case_id")
    private LegalCase legalCase;

    public Deadline() {
    }

    public Deadline(String title, LocalDate dueDate, DeadlinePriority priority) {
        this.title = title;
        this.dueDate = dueDate;
        this.priority = priority;
        this.completed = false;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public DeadlinePriority getPriority() {
        return priority;
    }

    public void setPriority(DeadlinePriority priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LegalCase getLegalCase() {
        return legalCase;
    }

    public void setLegalCase(LegalCase legalCase) {
        this.legalCase = legalCase;
    }
}