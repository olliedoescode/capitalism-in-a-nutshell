package com.simulation.backend.controller;

import com.simulation.backend.model.Company;
import com.simulation.backend.repository.CompanyRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private final CompanyRepository companyRepository;

    public SimulationController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @PostMapping("/init-world")
    @Transactional
    public ResponseEntity<WorldInitializationResponse> initializeWorld() {
        List<Company> existingCompanies = companyRepository.findAll();
        if (!existingCompanies.isEmpty()) {
            return ResponseEntity.ok(new WorldInitializationResponse(
                    false,
                    "World already initialized",
                    existingCompanies.stream().map(CompanyResponse::from).toList()));
        }

        List<Company> seededCompanies = companyRepository.saveAll(List.of(
                new Company(
                        "Apex Ventures",
                        true,
                        new BigDecimal("15000.00"),
                        "General Operations"),
                new Company(
                        "Titan Logistics",
                        false,
                        new BigDecimal("12000.00"),
                        "Bulk Shipping"),
                new Company(
                        "Nova Tech",
                        false,
                        new BigDecimal("12000.00"),
                        "R&D")));

        return ResponseEntity.ok(new WorldInitializationResponse(
                true,
                "World initialized successfully",
                seededCompanies.stream().map(CompanyResponse::from).toList()));
    }

    public record WorldInitializationResponse(
            boolean initialized,
            String message,
            List<CompanyResponse> companies) {
    }

    public record CompanyResponse(
            Long id,
            String name,
            boolean isPlayer,
            BigDecimal cashBalance,
            String specialty) {

        private static CompanyResponse from(Company company) {
            return new CompanyResponse(
                    company.getId(),
                    company.getName(),
                    company.isPlayer(),
                    company.getCashBalance(),
                    company.getSpecialty());
        }
    }
}
