package com.bfsi.risk;

import com.bfsi.risk.risk.RiskScoringService;
import com.bfsi.risk.rules.ApiLogAnalysisService;
import com.bfsi.risk.rules.DataQualityService;
import com.bfsi.risk.rules.IncidentAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validates every rule implementation against the dataset's own ground-truth
 * files in expected_outputs/ (row count minus header = expected flag count).
 */
@SpringBootTest
class RuleValidationTest {

    private static final Path EXPECTED_OUTPUTS = Path.of("../expected_outputs");

    @Autowired
    private DataQualityService dataQualityService;

    @Autowired
    private IncidentAnalysisService incidentAnalysisService;

    @Autowired
    private ApiLogAnalysisService apiLogAnalysisService;

    @Autowired
    private RiskScoringService riskScoringService;

    private static long expectedCount(String fileName) throws IOException {
        try (var lines = Files.lines(EXPECTED_OUTPUTS.resolve(fileName))) {
            return lines.count() - 1; // minus header
        }
    }

    @Test
    void invalidCustomerRelationshipMatchesGroundTruth() throws IOException {
        assertEquals(expectedCount("invalid_customer_relationship_transactions.csv"),
                dataQualityService.invalidCustomerRelationship().size());
    }

    @Test
    void negativeOrZeroAmountMatchesGroundTruth() throws IOException {
        assertEquals(expectedCount("negative_amount_transactions.csv"),
                dataQualityService.negativeOrZeroAmount().size());
    }

    @Test
    void futureDatedMatchesGroundTruth() throws IOException {
        assertEquals(expectedCount("future_dated_transactions.csv"),
                dataQualityService.futureDated().size());
    }

    @Test
    void closedAccountActivityMatchesGroundTruth() throws IOException {
        assertEquals(expectedCount("closed_account_transactions.csv"),
                dataQualityService.closedAccountActivity().size());
    }

    @Test
    void kycHighValueMatchesGroundTruth() throws IOException {
        assertEquals(expectedCount("kyc_high_value_transactions.csv"),
                dataQualityService.kycHighValue().size());
    }

    @Test
    void invalidCurrencyMatchesGroundTruth() throws IOException {
        assertEquals(expectedCount("invalid_currency_transactions.csv"),
                dataQualityService.invalidCurrency().size());
    }

    @Test
    void duplicateTransactionIdsMatchesGroundTruth() throws IOException {
        assertEquals(expectedCount("duplicate_transaction_ids.csv"),
                dataQualityService.duplicateTransactionIds().size());
    }

    @Test
    void highRiskTransactionsMatchesGroundTruth() throws IOException {
        assertEquals(expectedCount("expected_high_risk_transactions.csv"),
                riskScoringService.highRiskTransactions().size());
    }

    @Test
    void slaBreachesMatchesGroundTruth() throws IOException {
        assertEquals(expectedCount("sla_breached_incidents.csv"),
                incidentAnalysisService.slaBreaches().size());
    }

    @Test
    void statusDateConflictsMatchesGroundTruth() throws IOException {
        assertEquals(expectedCount("status_date_conflict_incidents.csv"),
                incidentAnalysisService.statusDateConflicts().size());
    }

    @Test
    void slowApiCallsMatchesGroundTruth() throws IOException {
        assertEquals(expectedCount("slow_api_logs_over_2000ms.csv"),
                apiLogAnalysisService.slowApiCalls().size());
    }

    @Test
    void serverFailuresMatchesGroundTruth() throws IOException {
        assertEquals(expectedCount("failed_api_logs_5xx.csv"),
                apiLogAnalysisService.serverFailures().size());
    }
}
