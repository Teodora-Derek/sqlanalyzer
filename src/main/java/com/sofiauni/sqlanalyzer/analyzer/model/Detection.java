package com.sofiauni.sqlanalyzer.analyzer.model;

import java.time.Instant;
import java.util.List;

/**
 * A positive classification produced by a {@code Detector}: a workload exhibited a
 * known inefficient access pattern (section 4.5).
 */
public record Detection(
        DetectionType type,
        Severity severity,
        String requestId,
        int occurrences,
        List<String> sampleSql,
        String message,
        Instant detectedAt
) {
}
