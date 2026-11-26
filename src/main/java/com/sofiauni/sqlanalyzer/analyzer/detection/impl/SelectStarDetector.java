package com.sofiauni.sqlanalyzer.analyzer.detection.impl;

import com.sofiauni.sqlanalyzer.analyzer.detection.RequestScopedDetector;
import com.sofiauni.sqlanalyzer.analyzer.model.Detection;
import com.sofiauni.sqlanalyzer.analyzer.model.DetectionType;
import com.sofiauni.sqlanalyzer.analyzer.model.QueryEvent;
import com.sofiauni.sqlanalyzer.analyzer.model.Severity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@code SELECT *} detector (section 4.5; Karwin's "Implicit Columns" anti-pattern). Selecting
 * all columns couples the consumer to the table layout and transfers unnecessary data.
 */
public class SelectStarDetector implements RequestScopedDetector {

    @Override
    public List<Detection> inspect(String requestId, List<QueryEvent> events) {
        List<Detection> detections = new ArrayList<>();
        Set<String> reported = new HashSet<>();

        for (QueryEvent event : events) {
            if (SqlPatterns.isSelectStar(event.normalizedSql()) && reported.add(event.sqlHash())) {
                detections.add(new Detection(
                        DetectionType.SELECT_STAR,
                        Severity.LOW,
                        requestId,
                        1,
                        List.of(event.normalizedSql()),
                        "Query selects all columns with SELECT *",
                        Instant.now()
                ));
            }
        }
        return detections;
    }
}
