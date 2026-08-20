package com.bfsi.risk.model;

import java.time.LocalDate;

public record Customer(
        String customerId,
        String customerName,
        String customerSegment,
        String ageGroup,
        String city,
        String state,
        String occupation,
        long annualIncomeInr,
        String riskCategory,
        String kycStatus,
        LocalDate customerSince,
        String customerStatus
) {
}
