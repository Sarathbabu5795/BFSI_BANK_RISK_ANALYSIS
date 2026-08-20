package com.bfsi.risk.model;

import java.time.LocalDateTime;

public record ApiLog(
        String logId,
        LocalDateTime timestamp,
        String apiName,
        String endpoint,
        String requestMethod,
        int responseCode,
        int responseTimeMs,
        int requestSizeBytes,
        int responseSizeBytes,
        String serverName,
        String environment,
        String errorCode,
        String timeoutFlag,
        String transactionId
) {
}
