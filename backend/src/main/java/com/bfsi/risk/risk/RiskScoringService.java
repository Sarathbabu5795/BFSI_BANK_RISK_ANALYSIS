package com.bfsi.risk.risk;

import com.bfsi.risk.config.AppProperties;
import com.bfsi.risk.dto.RiskAssessment;
import com.bfsi.risk.loader.CsvDataStore;
import com.bfsi.risk.model.Account;
import com.bfsi.risk.model.Customer;
import com.bfsi.risk.model.Transaction;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BR-011 (compare the dataset's ground-truth risk_score against fraud_flag to
 * surface label errors) plus a lightweight, dependency-free statistical
 * anomaly score: a weighted composite of amount-deviation-within-transaction-type,
 * customer risk category, KYC status, account status and transaction outcome.
 * This is the "Rules + AI Anomaly Detection" step of the pipeline for Phase 1 -
 * no LLM/embeddings call, so it stays fast and fully explainable.
 */
@Service
public class RiskScoringService {

    private final CsvDataStore store;
    private final AppProperties properties;

    private Map<String, double[]> amountStatsByType; // transactionType -> [mean, stdDev]

    public RiskScoringService(CsvDataStore store, AppProperties properties) {
        this.store = store;
        this.properties = properties;
    }

    @PostConstruct
    void computeAmountStats() {
        Map<String, List<Double>> amountsByType = new HashMap<>();
        for (Transaction t : store.transactions()) {
            amountsByType.computeIfAbsent(t.transactionType(), k -> new ArrayList<>())
                    .add(t.transactionAmount().doubleValue());
        }
        amountStatsByType = new HashMap<>();
        for (Map.Entry<String, List<Double>> e : amountsByType.entrySet()) {
            List<Double> values = e.getValue();
            double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double variance = values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0);
            amountStatsByType.put(e.getKey(), new double[]{mean, Math.sqrt(variance)});
        }
    }

    public List<RiskAssessment> assessAll() {
        List<RiskAssessment> result = new ArrayList<>(store.transactions().size());
        for (Transaction t : store.transactions()) {
            result.add(assess(t));
        }
        return result;
    }

    /** BR-011: risk_score >= threshold is expected to be high-risk. */
    public List<RiskAssessment> highRiskTransactions() {
        List<RiskAssessment> result = new ArrayList<>();
        for (Transaction t : store.transactions()) {
            if (t.riskScore() >= properties.highRiskScoreThreshold()) {
                result.add(assess(t));
            }
        }
        return result;
    }

    /** Rows where the expected-high-risk label disagrees with the recorded fraud_flag. */
    public List<RiskAssessment> labelMismatches() {
        List<RiskAssessment> result = new ArrayList<>();
        for (Transaction t : store.transactions()) {
            RiskAssessment assessment = assess(t);
            if (assessment.labelMismatch()) {
                result.add(assessment);
            }
        }
        return result;
    }

    public List<RiskAssessment> topAnomalies(int limit) {
        return assessAll().stream()
                .sorted(Comparator.comparingInt(RiskAssessment::computedAnomalyScore).reversed())
                .limit(limit)
                .toList();
    }

    private RiskAssessment assess(Transaction t) {
        Customer customer = store.customer(t.customerId());
        Account account = store.account(t.accountId());

        StringBuilder reason = new StringBuilder();
        int score = 0;

        double[] stats = amountStatsByType.getOrDefault(t.transactionType(), new double[]{0, 1});
        double stdDev = stats[1] == 0 ? 1 : stats[1];
        double zScore = (t.transactionAmount().doubleValue() - stats[0]) / stdDev;
        if (zScore > 1) {
            int points = (int) Math.min(40, Math.round(zScore * 10));
            score += points;
            reason.append("Amount is ").append(String.format("%.1f", zScore))
                    .append(" std-dev above the average ").append(t.transactionType()).append(" transaction. ");
        }

        if (customer != null) {
            if ("HIGH".equals(customer.riskCategory())) {
                score += 20;
                reason.append("Customer risk category is HIGH. ");
            } else if ("MEDIUM".equals(customer.riskCategory())) {
                score += 10;
            }
            if ("REJECTED".equals(customer.kycStatus()) || "EXPIRED".equals(customer.kycStatus())) {
                score += 15;
                reason.append("Customer KYC status is ").append(customer.kycStatus()).append(". ");
            } else if ("PENDING".equals(customer.kycStatus())) {
                score += 8;
            }
        }

        if (account != null) {
            if ("BLOCKED".equals(account.accountStatus()) || "CLOSED".equals(account.accountStatus())) {
                score += 15;
                reason.append("Account status is ").append(account.accountStatus()).append(". ");
            } else if ("DORMANT".equals(account.accountStatus())) {
                score += 8;
            }
        }

        if ("FAILED".equals(t.transactionStatus()) || "REVERSED".equals(t.transactionStatus())) {
            score += 10;
            reason.append("Transaction outcome was ").append(t.transactionStatus()).append(". ");
        }

        score = Math.max(0, Math.min(100, score));
        if (reason.isEmpty()) {
            reason.append("No anomaly signals detected; transaction fits normal patterns.");
        }

        boolean expectedHighRisk = t.riskScore() >= properties.highRiskScoreThreshold();
        boolean fraudFlagged = "Y".equals(t.fraudFlag());
        boolean labelMismatch = expectedHighRisk != fraudFlagged;

        return new RiskAssessment(t, t.riskScore(), score, expectedHighRisk, fraudFlagged, labelMismatch, reason.toString().trim());
    }
}
