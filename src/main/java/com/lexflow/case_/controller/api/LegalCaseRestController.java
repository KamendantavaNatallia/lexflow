package com.lexflow.case_.controller.api;

import com.lexflow.case_.dto.LegalCaseResponse;
import com.lexflow.case_.mapper.LegalCaseMapper;
import com.lexflow.case_.model.LegalCase;
import com.lexflow.case_.service.LegalCaseService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cases")
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
    public ResponseEntity<LegalCaseResponse> getCaseById(@PathVariable Long id) {
        LegalCase legalCase = legalCaseService.getCaseById(id);

        if (legalCase == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(legalCaseMapper.toResponse(legalCase));
    }
}
