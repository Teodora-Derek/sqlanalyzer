package com.sofiauni.sqlanalyzer.analyzer.detection.impl;

import com.sofiauni.sqlanalyzer.analyzer.detection.RequestScopedDetector;
import com.sofiauni.sqlanalyzer.analyzer.model.Detection;
import com.sofiauni.sqlanalyzer.analyzer.model.DetectionType;
import com.sofiauni.sqlanalyzer.analyzer.model.QueryEvent;
import com.sofiauni.sqlanalyzer.analyzer.model.QueryType;
import com.sofiauni.sqlanalyzer.analyzer.model.Severity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bulk-instead-of-batch detector (section 4.5). Many identical INSERT/UPDATE statements within
 * one request indicate row-by-row writes that should use JDBC batching.
 */
public class BulkInsteadOfBatchDetector implements RequestScopedDetector {

    private static final Set<QueryType> WRITE_TYPES = EnumSet.of(QueryType.INSERT, QueryType.UPDATE);

    private final int threshold;

    public BulkInsteadOfBatchDetector(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public List<Detection> inspect(String requestId, List<QueryEvent> events) {
        Map<String, List<QueryEvent>> byHash = new LinkedHashMap<>();
        for (QueryEvent event : events) {
            if (WRITE_TYPES.contains(event.queryType())) {
                byHash.computeIfAbsent(event.sqlHash(), key -> new ArrayList<>()).add(event);
            }
        }

        List<Detection> detections = new ArrayList<>();
        for (List<QueryEvent> group : byHash.values()) {
            if (group.size() >= threshold) {
                detections.add(new Detection(
                        DetectionType.BULK_INSTEAD_OF_BATCH,
                        Severity.MEDIUM,
                        requestId,
                        group.size(),
                        List.of(group.get(0).normalizedSql()),
                        "Same write statement executed %d times within one request".formatted(group.size()),
                        Instant.now()
                ));
            }
        }
        return detections;
    }
}
