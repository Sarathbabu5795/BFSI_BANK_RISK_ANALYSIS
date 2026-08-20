package com.bfsi.risk.dto;

import com.bfsi.risk.model.ApiLog;

public record ApiLogFlag(
        String ruleCode,
        String ruleName,
        ApiLog apiLog,
        String reason
) {
}
