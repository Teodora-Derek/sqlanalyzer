package com.sofiauni.sqlanalyzer.analyzer.detection.impl;

import com.sofiauni.sqlanalyzer.analyzer.detection.RequestScopedDetector;
import com.sofiauni.sqlanalyzer.analyzer.model.Detection;
import com.sofiauni.sqlanalyzer.analyzer.model.DetectionType;
import com.sofiauni.sqlanalyzer.analyzer.model.QueryEvent;
import com.sofiauni.sqlanalyzer.analyzer.model.QueryType;
import com.sofiauni.sqlanalyzer.analyzer.model.Severity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * N+1 query detector (section 4.5). Within one request, a single-row primary-key style
 * lookup ({@code ... WHERE <col> = ?}) repeated at or above the threshold signals that a
 * collection is being loaded one element at a time instead of with a join/batch fetch.
 */
public class NPlusOneDetector implements RequestScopedDetector {

    private final int threshold;

    public NPlusOneDetector(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public List<Detection> inspect(String requestId, List<QueryEvent> events) {
        Map<String, List<QueryEvent>> byHash = new LinkedHashMap<>();
        for (QueryEvent event : events) {
            if (event.queryType() == QueryType.SELECT && SqlPatterns.hasEqualityPredicate(event.normalizedSql())) {
                byHash.computeIfAbsent(event.sqlHash(), key -> new ArrayList<>()).add(event);
            }
        }

        List<Detection> detections = new ArrayList<>();
        for (List<QueryEvent> group : byHash.values()) {
            if (group.size() >= threshold) {
                detections.add(new Detection(
                        DetectionType.N_PLUS_ONE,
                        Severity.HIGH,
                        requestId,
                        group.size(),
                        List.of(group.get(0).normalizedSql()),
                        "Same single-row lookup executed %d times within one request".formatted(group.size()),
                        Instant.now()
                ));
            }
        }
        return detections;
    }
}
