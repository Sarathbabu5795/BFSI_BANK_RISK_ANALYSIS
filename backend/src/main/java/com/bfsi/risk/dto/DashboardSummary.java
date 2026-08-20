package com.bfsi.risk.dto;

import java.util.List;

public record DashboardSummary(
        long totalCustomers,
        long totalAccounts,
        long totalTransactions,
        long totalIncidents,
        long totalApiLogs,
        long flaggedTransactionCount,
        long highRiskTransactionCount,
        long labelMismatchCount,
        double averageRiskScore,
        long slaBreachCount,
        long statusDateConflictCount,
        long slowApiCount,
        long serverFailureCount,
        List<CategoryCount> flagsByRule,
        List<CategoryCount> riskCategoryDistribution,
        List<CategoryCount> incidentSeverityDistribution,
        List<CategoryCount> moduleHotspots
) {
}
