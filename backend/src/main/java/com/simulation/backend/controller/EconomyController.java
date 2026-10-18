package com.simulation.backend.controller;

import com.simulation.backend.model.Company;
import com.simulation.backend.repository.CompanyRepository;
import com.simulation.backend.service.EconomyService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/economy")
public class EconomyController {

    private final EconomyService economyService;
    private final CompanyRepository companyRepository;

    public EconomyController(
            EconomyService economyService,
            CompanyRepository companyRepository) {
        this.economyService = economyService;
        this.companyRepository = companyRepository;
    }

    @GetMapping("/market-state/{companyId}")
    public ResponseEntity<MarketStateResponse> getMarketState(@PathVariable Long companyId) {
        Company company = findCompany(companyId);
        List<CompetitorSummary> competitors = companyRepository.findAll().stream()
                .filter(candidate -> !candidate.getId().equals(companyId))
                .map(candidate -> new CompetitorSummary(
                        candidate.getId(),
                        companyName(candidate),
                        candidate.getCashBalance()))
                .toList();

        return ResponseEntity.ok(new MarketStateResponse(
                company.getId(),
                companyName(company),
                company.getCashBalance(),
                List.of(),
                competitors));
    }

    @PostMapping("/action")
    public ResponseEntity<ActionResponse> executeAction(
            @Valid @RequestBody ActionRequest action) {
        String actionType = action.actionType().trim().toUpperCase(Locale.ROOT);
        if (!actionType.equals("TRANSFER") && !actionType.equals("BUY_MATERIALS")) {
            return ResponseEntity.badRequest().body(new ActionResponse(
                    false,
                    "Unsupported action type: " + action.actionType(),
                    null));
        }
        if (action.targetCompanyId().equals(action.companyId())) {
            return ResponseEntity.badRequest().body(new ActionResponse(
                    false,
                    "Target company must differ from the acting company",
                    null));
        }

        boolean successful = economyService.executeTransfer(
                action.companyId(),
                action.targetCompanyId(),
                action.amount(),
                actionType);
        Company company = findCompany(action.companyId());

        return ResponseEntity.ok(new ActionResponse(
                successful,
                successful ? "Action completed" : "Insufficient funds",
                company.getCashBalance()));
    }

    private Company findCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Company not found: " + companyId));
    }

    private String companyName(Company company) {
        return company.getName() == null || company.getName().isBlank()
                ? "Company " + company.getId()
                : company.getName();
    }

    public record MarketStateResponse(
            Long companyId,
            String companyName,
            BigDecimal cashBalance,
            List<AssetSummary> assets,
            List<CompetitorSummary> competitors) {
    }

    public record AssetSummary(String name, BigDecimal value) {
    }

    public record CompetitorSummary(
            Long companyId,
            String companyName,
            BigDecimal cashBalance) {
    }

    public record ActionRequest(
            @NotNull Long companyId,
            @NotBlank String actionType,
            @NotNull Long targetCompanyId,
            @NotNull @DecimalMin(value = "0.0001") BigDecimal amount) {
    }

    public record ActionResponse(
            boolean success,
            String message,
            BigDecimal updatedCompanyBalance) {
    }
}
