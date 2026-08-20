package com.bfsi.risk.model;

import java.time.LocalDateTime;

public record Incident(
        String incidentId,
        String incidentTitle,
        String applicationModule,
        String severity,
        String priority,
        LocalDateTime reportedDatetime,
        String environment,
        String incidentStatus,
        String assignedTeam,
        String assignedEngineer,
        String rootCause,
        String resolutionSummary,
        LocalDateTime resolvedDatetime,
        int slaHours,
        String slaBreached,
        String relatedTransactionId,
        String relatedReleaseId
) {
}
