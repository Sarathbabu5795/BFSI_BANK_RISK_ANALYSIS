package com.bfsi.risk.loader;

import com.bfsi.risk.config.AppProperties;
import com.bfsi.risk.model.Account;
import com.bfsi.risk.model.ApiLog;
import com.bfsi.risk.model.ApplicationLog;
import com.bfsi.risk.model.Customer;
import com.bfsi.risk.model.Incident;
import com.bfsi.risk.model.ReferenceData;
import com.bfsi.risk.model.TestCase;
import com.bfsi.risk.model.Transaction;
import jakarta.annotation.PostConstruct;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.bfsi.risk.loader.CsvUtil.date;
import static com.bfsi.risk.loader.CsvUtil.dateTime;
import static com.bfsi.risk.loader.CsvUtil.decimal;
import static com.bfsi.risk.loader.CsvUtil.doubleVal;
import static com.bfsi.risk.loader.CsvUtil.integer;
import static com.bfsi.risk.loader.CsvUtil.longVal;
import static com.bfsi.risk.loader.CsvUtil.str;

/**
 * Loads every source CSV into memory once at startup. ~100k rows total across
 * all files - trivial for a JVM heap, so no database is needed for Phase 1.
 */
@Component
public class CsvDataStore {

    private static final Logger log = LoggerFactory.getLogger(CsvDataStore.class);

    private final AppProperties properties;

    private List<Customer> customers;
    private List<Account> accounts;
    private List<Transaction> transactions;
    private List<Incident> incidents;
    private List<ApiLog> apiLogs;
    private List<ApplicationLog> applicationLogs;
    private List<TestCase> testCases;
    private List<ReferenceData> referenceData;

    private Map<String, Customer> customerById;
    private Map<String, Account> accountById;

    public CsvDataStore(AppProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void load() throws IOException {
        Path dataDir = Path.of(properties.dataDir());
        log.info("Loading BFSI datasets from {}", dataDir.toAbsolutePath());

        customers = readAll(dataDir.resolve("customers.csv"), CsvDataStore::toCustomer);
        accounts = readAll(dataDir.resolve("accounts.csv"), CsvDataStore::toAccount);
        transactions = readAll(dataDir.resolve("transactions.csv"), CsvDataStore::toTransaction);
        incidents = readAll(dataDir.resolve("incidents.csv"), CsvDataStore::toIncident);
        apiLogs = readAll(dataDir.resolve("api_logs.csv"), CsvDataStore::toApiLog);
        applicationLogs = readAll(dataDir.resolve("application_logs.csv"), CsvDataStore::toApplicationLog);
        testCases = readAll(dataDir.resolve("test_cases.csv"), CsvDataStore::toTestCase);
        referenceData = readAll(dataDir.resolve("reference_data.csv"), CsvDataStore::toReferenceData);

        customerById = customers.stream().collect(Collectors.toMap(Customer::customerId, c -> c, (a, b) -> a));
        accountById = accounts.stream().collect(Collectors.toMap(Account::accountId, a -> a, (a, b) -> a));

        log.info("Loaded {} customers, {} accounts, {} transactions, {} incidents, {} api logs, {} app logs, {} test cases, {} reference rows",
                customers.size(), accounts.size(), transactions.size(), incidents.size(),
                apiLogs.size(), applicationLogs.size(), testCases.size(), referenceData.size());
    }

    private static <T> List<T> readAll(Path path, Function<CSVRecord, T> mapper) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
            List<T> result = new java.util.ArrayList<>();
            for (CSVRecord record : format.parse(reader)) {
                result.add(mapper.apply(record));
            }
            return List.copyOf(result);
        }
    }

    private static Customer toCustomer(CSVRecord r) {
        return new Customer(
                str(r.get("customer_id")), str(r.get("customer_name")), str(r.get("customer_segment")),
                str(r.get("age_group")), str(r.get("city")), str(r.get("state")), str(r.get("occupation")),
                longVal(r.get("annual_income_inr")), str(r.get("risk_category")), str(r.get("kyc_status")),
                date(r.get("customer_since")), str(r.get("customer_status")));
    }

    private static Account toAccount(CSVRecord r) {
        return new Account(
                str(r.get("account_id")), str(r.get("customer_id")), str(r.get("account_type")),
                str(r.get("branch_code")), date(r.get("opening_date")), decimal(r.get("current_balance")),
                decimal(r.get("available_balance")), str(r.get("currency")), str(r.get("account_status")),
                str(r.get("freeze_status")), date(r.get("last_transaction_date")));
    }

    private static Transaction toTransaction(CSVRecord r) {
        return new Transaction(
                str(r.get("transaction_id")), str(r.get("account_id")), str(r.get("customer_id")),
                dateTime(r.get("transaction_datetime")), str(r.get("transaction_type")), str(r.get("transaction_channel")),
                decimal(r.get("transaction_amount")), str(r.get("currency")), str(r.get("beneficiary_id")),
                str(r.get("source_location")), str(r.get("destination_location")), str(r.get("device_id")),
                str(r.get("ip_address")), str(r.get("transaction_status")), str(r.get("failure_reason")),
                str(r.get("settlement_status")), str(r.get("fraud_flag")), integer(r.get("risk_score")));
    }

    private static Incident toIncident(CSVRecord r) {
        return new Incident(
                str(r.get("incident_id")), str(r.get("incident_title")), str(r.get("application_module")),
                str(r.get("severity")), str(r.get("priority")), dateTime(r.get("reported_datetime")),
                str(r.get("environment")), str(r.get("incident_status")), str(r.get("assigned_team")),
                str(r.get("assigned_engineer")), str(r.get("root_cause")), str(r.get("resolution_summary")),
                dateTime(r.get("resolved_datetime")), integer(r.get("sla_hours")), str(r.get("sla_breached")),
                str(r.get("related_transaction_id")), str(r.get("related_release_id")));
    }

    private static ApiLog toApiLog(CSVRecord r) {
        return new ApiLog(
                str(r.get("log_id")), dateTime(r.get("timestamp")), str(r.get("api_name")), str(r.get("endpoint")),
                str(r.get("request_method")), integer(r.get("response_code")), integer(r.get("response_time_ms")),
                integer(r.get("request_size_bytes")), integer(r.get("response_size_bytes")), str(r.get("server_name")),
                str(r.get("environment")), str(r.get("error_code")), str(r.get("timeout_flag")), str(r.get("transaction_id")));
    }

    private static ApplicationLog toApplicationLog(CSVRecord r) {
        return new ApplicationLog(
                str(r.get("log_id")), dateTime(r.get("timestamp")), str(r.get("log_level")), str(r.get("application_module")),
                str(r.get("service_name")), str(r.get("server_name")), str(r.get("error_code")), str(r.get("error_message")),
                str(r.get("stack_trace")), str(r.get("user_id")), str(r.get("transaction_id")), str(r.get("correlation_id")));
    }

    private static TestCase toTestCase(CSVRecord r) {
        return new TestCase(
                str(r.get("test_case_id")), str(r.get("test_module")), str(r.get("test_scenario")), str(r.get("test_type")),
                str(r.get("priority")), str(r.get("expected_result")), str(r.get("automation_status")),
                date(r.get("last_execution_date")), str(r.get("execution_status")), str(r.get("failure_reason")),
                doubleVal(r.get("execution_time_seconds")), str(r.get("defect_id")));
    }

    private static ReferenceData toReferenceData(CSVRecord r) {
        return new ReferenceData(
                str(r.get("reference_type")), str(r.get("code")), str(r.get("description")),
                str(r.get("attribute_1")), str(r.get("attribute_2")));
    }

    public List<Customer> customers() {
        return customers;
    }

    public List<Account> accounts() {
        return accounts;
    }

    public List<Transaction> transactions() {
        return transactions;
    }

    public List<Incident> incidents() {
        return incidents;
    }

    public List<ApiLog> apiLogs() {
        return apiLogs;
    }

    public List<ApplicationLog> applicationLogs() {
        return applicationLogs;
    }

    public List<TestCase> testCases() {
        return testCases;
    }

    public List<ReferenceData> referenceData() {
        return referenceData;
    }

    public Customer customer(String customerId) {
        return customerById.get(customerId);
    }

    public Account account(String accountId) {
        return accountById.get(accountId);
    }
}
