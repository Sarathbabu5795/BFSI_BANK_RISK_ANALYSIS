package com.bfsi.risk.rules;

import com.bfsi.risk.config.AppProperties;
import com.bfsi.risk.dto.TransactionFlag;
import com.bfsi.risk.loader.CsvDataStore;
import com.bfsi.risk.model.Account;
import com.bfsi.risk.model.Customer;
import com.bfsi.risk.model.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Rule-based data quality checks (BR-001, BR-002, BR-003, BR-004, BR-005, BR-006
 * from docs/01_Data_Dictionary_and_Ground_Truth.xlsx) plus duplicate transaction
 * ID detection. Every check produces a plain-English reason grounded in the
 * actual matched fields - this is the "explainable insights" requirement,
 * satisfied deterministically rather than via an LLM.
 */
@Service
public class DataQualityService {

    private final CsvDataStore store;
    private final AppProperties properties;
    private final Set<String> validCurrencies;

    public DataQualityService(CsvDataStore store, AppProperties properties) {
        this.store = store;
        this.properties = properties;
        this.validCurrencies = Set.copyOf(properties.validCurrencies());
    }

    /** BR-001: transaction.customer_id must exist in customers.csv. */
    public List<TransactionFlag> invalidCustomerRelationship() {
        List<TransactionFlag> flags = new ArrayList<>();
        for (Transaction t : store.transactions()) {
            Customer customer = store.customer(t.customerId());
            if (customer == null) {
                flags.add(new TransactionFlag("BR-001", "Invalid customer relationship", t,
                        "customer_id " + t.customerId() + " referenced by this transaction does not exist in customers.csv"));
            }
        }
        return flags;
    }

    /** BR-002: transaction amount must be greater than zero. */
    public List<TransactionFlag> negativeOrZeroAmount() {
        List<TransactionFlag> flags = new ArrayList<>();
        for (Transaction t : store.transactions()) {
            if (t.transactionAmount().compareTo(BigDecimal.ZERO) <= 0) {
                flags.add(new TransactionFlag("BR-002", "Negative or zero amount", t,
                        "Transaction amount " + t.transactionAmount() + " is not greater than zero"));
            }
        }
        return flags;
    }

    /** BR-003: transaction datetime must not be later than the dataset's reporting period. */
    public List<TransactionFlag> futureDated() {
        List<TransactionFlag> flags = new ArrayList<>();
        for (Transaction t : store.transactions()) {
            if (t.transactionDatetime() != null && t.transactionDatetime().isAfter(properties.reportingCutoff())) {
                flags.add(new TransactionFlag("BR-003", "Future-dated transaction", t,
                        "Transaction dated " + t.transactionDatetime() + " is after the reporting cutoff "
                                + properties.reportingCutoff()));
            }
        }
        return flags;
    }

    /** BR-004: successful transactions should not be processed on CLOSED accounts. */
    public List<TransactionFlag> closedAccountActivity() {
        List<TransactionFlag> flags = new ArrayList<>();
        for (Transaction t : store.transactions()) {
            Account account = store.account(t.accountId());
            if (account != null && "CLOSED".equals(account.accountStatus()) && "SUCCESS".equals(t.transactionStatus())) {
                flags.add(new TransactionFlag("BR-004", "Activity on closed account", t,
                        "Transaction succeeded on account " + t.accountId() + " which has status CLOSED"));
            }
        }
        return flags;
    }

    /** BR-005: high-value transactions from REJECTED or EXPIRED KYC customers require escalation. */
    public List<TransactionFlag> kycHighValue() {
        List<TransactionFlag> flags = new ArrayList<>();
        for (Transaction t : store.transactions()) {
            Customer customer = store.customer(t.customerId());
            if (customer == null) {
                continue;
            }
            boolean badKyc = "REJECTED".equals(customer.kycStatus()) || "EXPIRED".equals(customer.kycStatus());
            if (badKyc && t.transactionAmount().compareTo(properties.kycHighValueThreshold()) > 0) {
                flags.add(new TransactionFlag("BR-005", "High-value transfer with invalid KYC", t,
                        "Amount " + t.transactionAmount() + " exceeds the " + properties.kycHighValueThreshold()
                                + " threshold and customer KYC status is " + customer.kycStatus()));
            }
        }
        return flags;
    }

    /** BR-006: valid currencies are INR, USD, EUR, GBP. */
    public List<TransactionFlag> invalidCurrency() {
        List<TransactionFlag> flags = new ArrayList<>();
        for (Transaction t : store.transactions()) {
            if (!validCurrencies.contains(t.currency())) {
                flags.add(new TransactionFlag("BR-006", "Invalid currency code", t,
                        "Currency code '" + t.currency() + "' is not one of " + validCurrencies));
            }
        }
        return flags;
    }

    /** Duplicate business keys: same transaction_id appearing more than once. */
    public List<TransactionFlag> duplicateTransactionIds() {
        Map<String, List<Transaction>> byId = new LinkedHashMap<>();
        for (Transaction t : store.transactions()) {
            byId.computeIfAbsent(t.transactionId(), k -> new ArrayList<>()).add(t);
        }
        List<TransactionFlag> flags = new ArrayList<>();
        for (Map.Entry<String, List<Transaction>> entry : byId.entrySet()) {
            if (entry.getValue().size() > 1) {
                flags.add(new TransactionFlag("DUP-ID", "Duplicate transaction ID", entry.getValue().get(0),
                        "transaction_id " + entry.getKey() + " appears " + entry.getValue().size() + " times in transactions.csv"));
            }
        }
        return flags;
    }

    public List<TransactionFlag> allFlags() {
        List<TransactionFlag> flags = new ArrayList<>();
        flags.addAll(invalidCustomerRelationship());
        flags.addAll(negativeOrZeroAmount());
        flags.addAll(futureDated());
        flags.addAll(closedAccountActivity());
        flags.addAll(kycHighValue());
        flags.addAll(invalidCurrency());
        flags.addAll(duplicateTransactionIds());
        return flags;
    }
}
