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
 * Repeated identical query detector (section 4.5). The same SELECT executed multiple times within
 * one request indicates a missing request-scoped or first-level cache. Groups that qualify as N+1
 * (a primary-key lookup repeated at or above the N+1 threshold) are deferred to that detector to
 * avoid double-reporting the same root cause.
 */
public class RepeatedQueryDetector implements RequestScopedDetector {

    private final int threshold;
    private final int nPlusOneThreshold;

    public RepeatedQueryDetector(int threshold, int nPlusOneThreshold) {
        this.threshold = threshold;
        this.nPlusOneThreshold = nPlusOneThreshold;
    }

    @Override
    public List<Detection> inspect(String requestId, List<QueryEvent> events) {
        Map<String, List<QueryEvent>> byHash = new LinkedHashMap<>();
        for (QueryEvent event : events) {
            if (event.queryType() == QueryType.SELECT) {
                byHash.computeIfAbsent(event.sqlHash(), key -> new ArrayList<>()).add(event);
            }
        }

        List<Detection> detections = new ArrayList<>();
        for (List<QueryEvent> group : byHash.values()) {
            if (group.size() < threshold || claimedByNPlusOne(group)) {
                continue;
            }
            detections.add(new Detection(
                    DetectionType.REPEATED_QUERY,
                    Severity.MEDIUM,
                    requestId,
                    group.size(),
                    List.of(group.get(0).normalizedSql()),
                    "Identical query executed %d times within one request".formatted(group.size()),
                    Instant.now()
            ));
        }
        return detections;
    }

    private boolean claimedByNPlusOne(List<QueryEvent> group) {
        return group.size() >= nPlusOneThreshold && SqlPatterns.hasEqualityPredicate(group.get(0).normalizedSql());
    }
}
