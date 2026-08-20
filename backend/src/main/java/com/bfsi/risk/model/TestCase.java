package com.bfsi.risk.model;

import java.time.LocalDate;

public record TestCase(
        String testCaseId,
        String testModule,
        String testScenario,
        String testType,
        String priority,
        String expectedResult,
        String automationStatus,
        LocalDate lastExecutionDate,
        String executionStatus,
        String failureReason,
        double executionTimeSeconds,
        String defectId
) {
}
