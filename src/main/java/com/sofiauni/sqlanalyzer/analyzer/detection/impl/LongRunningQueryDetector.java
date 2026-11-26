package com.sofiauni.sqlanalyzer.analyzer.detection.impl;

import com.sofiauni.sqlanalyzer.analyzer.aggregation.Aggregator;
import com.sofiauni.sqlanalyzer.analyzer.detection.AggregateDetector;
import com.sofiauni.sqlanalyzer.analyzer.model.Detection;
import com.sofiauni.sqlanalyzer.analyzer.model.DetectionType;
import com.sofiauni.sqlanalyzer.analyzer.model.Severity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Long-running query detector (section 4.5). Runs over the sliding window: if a query shape's
 * p95 latency exceeds the threshold it is flagged for query-plan investigation.
 */
public class LongRunningQueryDetector implements AggregateDetector {

    private final long thresholdNanos;

    public LongRunningQueryDetector(long thresholdMillis) {
        this.thresholdNanos = thresholdMillis * 1_000_000L;
    }

    @Override
    public List<Detection> inspect(Aggregator aggregator) {
        List<Detection> detections = new ArrayList<>();
        for (Aggregator.HashStats stats : aggregator.windowStats()) {
            if (stats.p95Nanos() >= thresholdNanos) {
                detections.add(new Detection(
                        DetectionType.LONG_RUNNING_QUERY,
                        Severity.MEDIUM,
                        null,
                        stats.count(),
                        List.of(stats.sampleSql() == null ? "(unknown)" : stats.sampleSql()),
                        "Query p95 latency is %d ms over the window".formatted(stats.p95Nanos() / 1_000_000L),
                        Instant.now()
                ));
            }
        }
        return detections;
    }
}
