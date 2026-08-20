package com.bfsi.risk.web;

import com.bfsi.risk.dto.RiskAssessment;
import com.bfsi.risk.dto.TransactionFlag;
import com.bfsi.risk.risk.RiskScoringService;
import com.bfsi.risk.rules.DataQualityService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:4200")
public class TransactionController {

    private final DataQualityService dataQualityService;
    private final RiskScoringService riskScoringService;

    public TransactionController(DataQualityService dataQualityService, RiskScoringService riskScoringService) {
        this.dataQualityService = dataQualityService;
        this.riskScoringService = riskScoringService;
    }

    @GetMapping("/flagged")
    public List<TransactionFlag> flagged(@RequestParam(required = false) String rule) {
        List<TransactionFlag> all = dataQualityService.allFlags();
        if (rule == null || rule.isBlank()) {
            return all;
        }
        return all.stream().filter(f -> f.ruleCode().equalsIgnoreCase(rule)).toList();
    }

    @GetMapping("/high-risk")
    public List<RiskAssessment> highRisk() {
        return riskScoringService.highRiskTransactions();
    }

    @GetMapping("/label-mismatches")
    public List<RiskAssessment> labelMismatches() {
        return riskScoringService.labelMismatches();
    }

    @GetMapping("/top-anomalies")
    public List<RiskAssessment> topAnomalies(@RequestParam(defaultValue = "50") int limit) {
        if (limit < 1 || limit > 5000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be between 1 and 5000");
        }
        return riskScoringService.topAnomalies(limit);
    }
}
