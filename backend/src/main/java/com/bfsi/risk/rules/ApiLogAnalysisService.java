package com.bfsi.risk.rules;

import com.bfsi.risk.config.AppProperties;
import com.bfsi.risk.dto.ApiLogFlag;
import com.bfsi.risk.loader.CsvDataStore;
import com.bfsi.risk.model.ApiLog;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** BR-009 (slow APIs) and BR-010 (5xx server failures). */
@Service
public class ApiLogAnalysisService {

    private final CsvDataStore store;
    private final AppProperties properties;

    public ApiLogAnalysisService(CsvDataStore store, AppProperties properties) {
        this.store = store;
        this.properties = properties;
    }

    /** BR-009: API latency above 2000 ms is slow. */
    public List<ApiLogFlag> slowApiCalls() {
        List<ApiLogFlag> flags = new ArrayList<>();
        for (ApiLog a : store.apiLogs()) {
            if (a.responseTimeMs() > properties.slowApiThresholdMs()) {
                flags.add(new ApiLogFlag("BR-009", "Slow API response", a,
                        a.apiName() + " " + a.endpoint() + " took " + a.responseTimeMs() + "ms (> "
                                + properties.slowApiThresholdMs() + "ms threshold)"));
            }
        }
        return flags;
    }

    /** BR-010: HTTP 5xx responses are server-side failures. */
    public List<ApiLogFlag> serverFailures() {
        List<ApiLogFlag> flags = new ArrayList<>();
        for (ApiLog a : store.apiLogs()) {
            if (a.responseCode() >= 500) {
                flags.add(new ApiLogFlag("BR-010", "Server failure (5xx)", a,
                        a.apiName() + " " + a.endpoint() + " returned HTTP " + a.responseCode()
                                + (a.errorCode().isBlank() ? "" : " (" + a.errorCode() + ")")));
            }
        }
        return flags;
    }

    /** Which API endpoints fail most often - a technical hotspot signal. */
    public List<Map.Entry<String, Long>> topFailingApis(int limit) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (ApiLog a : store.apiLogs()) {
            if (a.responseCode() >= 400) {
                counts.merge(a.apiName(), 1L, Long::sum);
            }
        }
        return counts.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Long>>comparingLong(Map.Entry::getValue).reversed())
                .limit(limit)
                .toList();
    }
}
