package com.bfsi.risk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        String dataDir,
        LocalDateTime reportingCutoff,
        List<String> validCurrencies,
        BigDecimal kycHighValueThreshold,
        int highRiskScoreThreshold,
        int slowApiThresholdMs
) {
}
