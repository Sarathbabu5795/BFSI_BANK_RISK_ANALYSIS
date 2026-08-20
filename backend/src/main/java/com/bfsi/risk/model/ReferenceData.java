package com.bfsi.risk.model;

public record ReferenceData(
        String referenceType,
        String code,
        String description,
        String attribute1,
        String attribute2
) {
}
