package com.bfsi.risk.dto;

import com.bfsi.risk.model.Transaction;

public record TransactionFlag(
        String ruleCode,
        String ruleName,
        Transaction transaction,
        String reason
) {
}
