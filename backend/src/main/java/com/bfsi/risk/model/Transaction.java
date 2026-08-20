package com.bfsi.risk.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Transaction(
        String transactionId,
        String accountId,
        String customerId,
        LocalDateTime transactionDatetime,
        String transactionType,
        String transactionChannel,
        BigDecimal transactionAmount,
        String currency,
        String beneficiaryId,
        String sourceLocation,
        String destinationLocation,
        String deviceId,
        String ipAddress,
        String transactionStatus,
        String failureReason,
        String settlementStatus,
        String fraudFlag,
        int riskScore
) {
}
