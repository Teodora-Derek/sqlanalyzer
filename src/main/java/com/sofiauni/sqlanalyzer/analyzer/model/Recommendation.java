package com.sofiauni.sqlanalyzer.analyzer.model;

import java.time.Instant;
import java.util.List;

/**
 * A structured, actionable suggestion derived from a {@link Detection} (section 4.6 "Output").
 */
public record Recommendation(
        DetectionType type,
        Severity severity,
        String requestId,
        String description,
        String suggestedAction,
        List<String> sampleSql,
        Instant createdAt
) {
}
