package com.sofiauni.sqlanalyzer.analyzer.model;

/**
 * Minimal, immutable observation captured for a single executed SQL statement,
 * as defined in the methodology (section 4.2 "Input Data").
 */
public record QueryEvent(
        String rawSql,
        String normalizedSql,
        String sqlHash,
        QueryType queryType,
        long durationNs,
        long rowCount,
        long timestampNs,
        String transactionId,
        String requestId,
        String stackHint
) {
}
