package com.sofiauni.sqlanalyzer.analyzer.recommendation;

import com.sofiauni.sqlanalyzer.analyzer.metrics.AnalyzerMetrics;
import com.sofiauni.sqlanalyzer.analyzer.model.Detection;
import com.sofiauni.sqlanalyzer.analyzer.model.DetectionType;
import com.sofiauni.sqlanalyzer.analyzer.model.Recommendation;
import com.sofiauni.sqlanalyzer.analyzer.report.ReportSink;
import com.sofiauni.sqlanalyzer.analyzer.report.ReportStore;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Turns a {@link Detection} into an actionable {@link Recommendation} (section 4.6), stores it,
 * exports it to the optional sink and records a metric. Recent duplicate detections are
 * suppressed to avoid flooding the report with the same finding.
 */
public class RecommendationEngine {

    private static final long DEDUP_WINDOW_MS = 60_000;

    private final ReportStore reportStore;
    private final ReportSink reportSink;
    private final AnalyzerMetrics metrics;
    private final Map<String, Long> recentlyEmitted = new ConcurrentHashMap<>();

    public RecommendationEngine(ReportStore reportStore, ReportSink reportSink, AnalyzerMetrics metrics) {
        this.reportStore = reportStore;
        this.reportSink = reportSink;
        this.metrics = metrics;
    }

    public void handle(Detection detection) {
        if (isDuplicate(detection)) {
            return;
        }

        Advice advice = adviceFor(detection.type());
        Recommendation recommendation = new Recommendation(
                detection.type(),
                detection.severity(),
                detection.requestId(),
                advice.description(),
                advice.action(),
                detection.sampleSql(),
                Instant.now()
        );

        reportStore.add(recommendation);
        reportSink.export(recommendation);
        metrics.recordDetection(detection.type());
    }

    private boolean isDuplicate(Detection detection) {
        String key = detection.type() + "|" + String.join(";", detection.sampleSql());
        long now = System.currentTimeMillis();
        Long previous = recentlyEmitted.put(key, now);
        return previous != null && (now - previous) < DEDUP_WINDOW_MS;
    }

    private record Advice(String description, String action) {
    }

    private Advice adviceFor(DetectionType type) {
        return switch (type) {
            case N_PLUS_ONE -> new Advice(
                    "N+1 query pattern: an association is loaded one row at a time.",
                    "Use JOIN FETCH, @EntityGraph or batch fetching to load the association in a single query.");
            case REPEATED_QUERY -> new Advice(
                    "The same query is executed multiple times within one request.",
                    "Cache the result at request scope or rely on the first-level (persistence) cache.");
            case MISSING_PAGINATION -> new Advice(
                    "A SELECT returns an unbounded result set.",
                    "Add pagination using LIMIT/OFFSET or keyset pagination.");
            case SELECT_STAR -> new Advice(
                    "Query uses SELECT * and retrieves all columns.",
                    "Project only the columns you need into an explicit DTO.");
            case LONG_RUNNING_QUERY -> new Advice(
                    "Query p95 latency exceeds the configured threshold.",
                    "Inspect the query plan; review indexes, statistics and the query shape.");
            case BULK_INSTEAD_OF_BATCH -> new Advice(
                    "Many identical write statements are executed individually.",
                    "Use JDBC batch updates or a bulk insert.");
            case TRANSACTION_SPAN_ANOMALY -> new Advice(
                    "A transaction spans an unusually large number of statements.",
                    "Consider splitting the transaction; avoid holding it open across many operations.");
        };
    }
}
