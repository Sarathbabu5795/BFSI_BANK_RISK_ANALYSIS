package com.bfsi.risk.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Account(
        String accountId,
        String customerId,
        String accountType,
        String branchCode,
        LocalDate openingDate,
        BigDecimal currentBalance,
        BigDecimal availableBalance,
        String currency,
        String accountStatus,
        String freezeStatus,
        LocalDate lastTransactionDate
) {
}
