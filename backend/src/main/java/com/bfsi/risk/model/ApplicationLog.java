package com.bfsi.risk.model;

import java.time.LocalDateTime;

public record ApplicationLog(
        String logId,
        LocalDateTime timestamp,
        String logLevel,
        String applicationModule,
        String serviceName,
        String serverName,
        String errorCode,
        String errorMessage,
        String stackTrace,
        String userId,
        String transactionId,
        String correlationId
) {
}
