package com.bfsi.risk.dto;

import com.bfsi.risk.model.Incident;

public record IncidentFlag(
        String ruleCode,
        String ruleName,
        Incident incident,
        String reason
) {
}
