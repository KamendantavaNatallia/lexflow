package com.lexflow.deadline.controller.api;

import com.lexflow.deadline.dto.DeadlineRequest;
import com.lexflow.deadline.dto.DeadlineResponse;
import com.lexflow.deadline.mapper.DeadlineMapper;
import com.lexflow.deadline.model.Deadline;
import com.lexflow.deadline.service.DeadlineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST API for deadlines.
 *
 * A deadline always belongs to a case, so listing and creating are nested under the case:
 *   GET  /api/v1/cases/{caseId}/deadlines
 *   POST /api/v1/cases/{caseId}/deadlines
 * Once it exists, a deadline has its own id and is addressed directly:
 *   GET/PUT/DELETE /api/v1/deadlines/{id},  PATCH /api/v1/deadlines/{id}/complete
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Deadlines", description = "Court and contract deadlines attached to a case")
public class DeadlineRestController {

    private final DeadlineService deadlineService;
    private final DeadlineMapper deadlineMapper;

    public DeadlineRestController(DeadlineService deadlineService, DeadlineMapper deadlineMapper) {
        this.deadlineService = deadlineService;
        this.deadlineMapper = deadlineMapper;
    }

    @GetMapping("/cases/{caseId}/deadlines")
    @Operation(summary = "List deadlines of a case, earliest due date first")
    public List<DeadlineResponse> getDeadlinesForCase(@PathVariable Long caseId) {
        return deadlineService.getDeadlinesForCase(caseId).stream()
                .map(deadlineMapper::toResponse)
                .toList();
    }

    @PostMapping("/cases/{caseId}/deadlines")
    @Operation(summary = "Add a deadline to a case (ADMIN)")
    public ResponseEntity<DeadlineResponse> createDeadline(
            @PathVariable Long caseId,
            @Valid @RequestBody DeadlineRequest request
    ) {
        Deadline created = deadlineService.createDeadline(caseId, deadlineMapper.toEntity(request));

        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/deadlines/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(deadlineMapper.toResponse(created));
    }

    @GetMapping("/deadlines/overdue")
    @Operation(summary = "All open deadlines whose due date has passed")
    public List<DeadlineResponse> getOverdueDeadlines() {
        return deadlineService.getOverdueDeadlines().stream()
                .map(deadlineMapper::toResponse)
                .toList();
    }

    @GetMapping("/deadlines/upcoming")
    @Operation(summary = "All open deadlines due today or later")
    public List<DeadlineResponse> getUpcomingDeadlines() {
        return deadlineService.getUpcomingDeadlines().stream()
                .map(deadlineMapper::toResponse)
                .toList();
    }

    @GetMapping("/deadlines/{id}")
    @Operation(summary = "Get one deadline")
    public DeadlineResponse getDeadline(@PathVariable Long id) {
        return deadlineMapper.toResponse(deadlineService.getRequiredDeadline(id));
    }

    @PutMapping("/deadlines/{id}")
    @Operation(summary = "Update title, due date and priority (ADMIN)")
    public DeadlineResponse updateDeadline(
            @PathVariable Long id,
            @Valid @RequestBody DeadlineRequest request
    ) {
        Deadline updated = deadlineService.updateDeadline(id, deadlineMapper.toEntity(request));
        return deadlineMapper.toResponse(updated);
    }

    @PatchMapping("/deadlines/{id}/complete")
    @Operation(summary = "Mark a deadline as completed (ADMIN)")
    public DeadlineResponse completeDeadline(@PathVariable Long id) {
        return deadlineMapper.toResponse(deadlineService.completeDeadline(id));
    }

    @DeleteMapping("/deadlines/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a deadline (ADMIN)")
    public void deleteDeadline(@PathVariable Long id) {
        deadlineService.deleteRequiredDeadline(id);
    }
}
