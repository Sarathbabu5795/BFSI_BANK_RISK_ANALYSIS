package com.bfsi.risk.dto;

import com.bfsi.risk.model.Transaction;

public record RiskAssessment(
        Transaction transaction,
        int providedRiskScore,
        int computedAnomalyScore,
        boolean expectedHighRisk,
        boolean fraudFlagged,
        boolean labelMismatch,
        String reason
) {
}
