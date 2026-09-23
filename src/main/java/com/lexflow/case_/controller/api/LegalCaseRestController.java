package com.lexflow.case_.controller.api;

import com.lexflow.case_.dto.LegalCaseRequest;
import com.lexflow.case_.dto.LegalCaseResponse;
import com.lexflow.case_.mapper.LegalCaseMapper;
import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.service.LegalCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * REST API for legal cases.
 *
 * GET    /api/v1/cases        list with search, status filter, sorting, paging
 * GET    /api/v1/cases/{id}   one case                     200 / 404
 * POST   /api/v1/cases        create                       201 + Location header / 400
 * PUT    /api/v1/cases/{id}   full update                  200 / 400 / 404
 * DELETE /api/v1/cases/{id}   delete (with its deadlines,  204 / 404
 *                             notes and documents)
 *
 * Access rules live in SecurityConfig: USER can read, ADMIN can also write.
 */
@RestController
@RequestMapping("/api/v1/cases")
@Tag(name = "Cases", description = "Legal cases: search, create, update, delete")
public class LegalCaseRestController {

    private final LegalCaseService legalCaseService;
    private final LegalCaseMapper legalCaseMapper;

    public LegalCaseRestController(
            LegalCaseService legalCaseService,
            LegalCaseMapper legalCaseMapper
    ) {
        this.legalCaseService = legalCaseService;
        this.legalCaseMapper = legalCaseMapper;
    }

    @GetMapping
    @Operation(summary = "Search cases by keyword and status, with sorting and paging")
    public Page<LegalCaseResponse> getCases(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "5") int size
    ) {
        int pageSize = switch (size) {
            case 10, 20 -> size;
            default -> 5;
        };

        return legalCaseService
                .searchCases(keyword, status, sort, page, pageSize)
                .map(legalCaseMapper::toResponse);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one case")
    public LegalCaseResponse getCaseById(@PathVariable Long id) {
        return legalCaseMapper.toResponse(legalCaseService.getRequiredCase(id));
    }

    @PostMapping
    @Operation(summary = "Create a case (ADMIN)")
    public ResponseEntity<LegalCaseResponse> createCase(@Valid @RequestBody LegalCaseRequest request) {
        LegalCase created = legalCaseService.createCase(legalCaseMapper.toEntity(request));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(legalCaseMapper.toResponse(created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a case (ADMIN)")
    public LegalCaseResponse updateCase(
            @PathVariable Long id,
            @Valid @RequestBody LegalCaseRequest request
    ) {
        LegalCase updated = legalCaseService.updateCase(id, legalCaseMapper.toEntity(request));
        return legalCaseMapper.toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a case with its deadlines, notes and documents (ADMIN)")
    public void deleteCase(@PathVariable Long id) {
        legalCaseService.deleteRequiredCase(id);
    }
}
