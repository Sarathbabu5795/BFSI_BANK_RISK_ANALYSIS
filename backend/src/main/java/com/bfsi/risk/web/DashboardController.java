package com.bfsi.risk.web;

import com.bfsi.risk.dto.CategoryCount;
import com.bfsi.risk.dto.DashboardSummary;
import com.bfsi.risk.dto.TransactionFlag;
import com.bfsi.risk.loader.CsvDataStore;
import com.bfsi.risk.model.Customer;
import com.bfsi.risk.model.Incident;
import com.bfsi.risk.model.Transaction;
import com.bfsi.risk.risk.RiskScoringService;
import com.bfsi.risk.rules.ApiLogAnalysisService;
import com.bfsi.risk.rules.DataQualityService;
import com.bfsi.risk.rules.IncidentAnalysisService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {

    private final CsvDataStore store;
    private final DataQualityService dataQualityService;
    private final IncidentAnalysisService incidentAnalysisService;
    private final ApiLogAnalysisService apiLogAnalysisService;
    private final RiskScoringService riskScoringService;

    public DashboardController(CsvDataStore store, DataQualityService dataQualityService,
                                IncidentAnalysisService incidentAnalysisService,
                                ApiLogAnalysisService apiLogAnalysisService,
                                RiskScoringService riskScoringService) {
        this.store = store;
        this.dataQualityService = dataQualityService;
        this.incidentAnalysisService = incidentAnalysisService;
        this.apiLogAnalysisService = apiLogAnalysisService;
        this.riskScoringService = riskScoringService;
    }

    @GetMapping("/summary")
    public DashboardSummary summary() {
        List<TransactionFlag> flags = dataQualityService.allFlags();
        Map<String, Long> byRule = new LinkedHashMap<>();
        for (TransactionFlag f : flags) {
            byRule.merge(f.ruleName(), 1L, Long::sum);
        }
        List<CategoryCount> flagsByRule = byRule.entrySet().stream()
                .map(e -> new CategoryCount(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(CategoryCount::count).reversed())
                .toList();

        Map<String, Long> byRiskCategory = new LinkedHashMap<>();
        for (Customer c : store.customers()) {
            byRiskCategory.merge(c.riskCategory(), 1L, Long::sum);
        }
        List<CategoryCount> riskCategoryDistribution = byRiskCategory.entrySet().stream()
                .map(e -> new CategoryCount(e.getKey(), e.getValue()))
                .toList();

        Map<String, Long> bySeverity = new LinkedHashMap<>();
        for (Incident i : store.incidents()) {
            bySeverity.merge(i.severity(), 1L, Long::sum);
        }
        List<CategoryCount> incidentSeverityDistribution = bySeverity.entrySet().stream()
                .map(e -> new CategoryCount(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(CategoryCount::category))
                .toList();

        List<CategoryCount> moduleHotspots = incidentAnalysisService.moduleHotspots(6).stream()
                .map(e -> new CategoryCount(e.getKey(), e.getValue()))
                .toList();

        double avgRiskScore = store.transactions().stream()
                .mapToInt(Transaction::riskScore)
                .average()
                .orElse(0);

        return new DashboardSummary(
                store.customers().size(),
                store.accounts().size(),
                store.transactions().size(),
                store.incidents().size(),
                store.apiLogs().size(),
                flags.size(),
                riskScoringService.highRiskTransactions().size(),
                riskScoringService.labelMismatches().size(),
                Math.round(avgRiskScore * 100) / 100.0,
                incidentAnalysisService.slaBreaches().size(),
                incidentAnalysisService.statusDateConflicts().size(),
                apiLogAnalysisService.slowApiCalls().size(),
                apiLogAnalysisService.serverFailures().size(),
                flagsByRule,
                riskCategoryDistribution,
                incidentSeverityDistribution,
                moduleHotspots
        );
    }
}
