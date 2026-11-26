package com.sofiauni.sqlanalyzer.analyzer.detection.impl;

import com.sofiauni.sqlanalyzer.analyzer.detection.RequestScopedDetector;
import com.sofiauni.sqlanalyzer.analyzer.model.Detection;
import com.sofiauni.sqlanalyzer.analyzer.model.DetectionType;
import com.sofiauni.sqlanalyzer.analyzer.model.QueryEvent;
import com.sofiauni.sqlanalyzer.analyzer.model.Severity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Transaction span anomaly detector (section 4.5). A very large number of statements grouped
 * under one transaction (connection) suggests a long-lived transaction holding resources.
 */
public class TransactionSpanAnomalyDetector implements RequestScopedDetector {

    private final int threshold;

    public TransactionSpanAnomalyDetector(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public List<Detection> inspect(String requestId, List<QueryEvent> events) {
        Map<String, List<QueryEvent>> byTransaction = new LinkedHashMap<>();
        for (QueryEvent event : events) {
            if (event.transactionId() != null) {
                byTransaction.computeIfAbsent(event.transactionId(), key -> new ArrayList<>()).add(event);
            }
        }

        List<Detection> detections = new ArrayList<>();
        for (List<QueryEvent> group : byTransaction.values()) {
            if (group.size() >= threshold) {
                detections.add(new Detection(
                        DetectionType.TRANSACTION_SPAN_ANOMALY,
                        Severity.MEDIUM,
                        requestId,
                        group.size(),
                        List.of(group.get(0).normalizedSql()),
                        "Transaction spans %d statements".formatted(group.size()),
                        Instant.now()
                ));
            }
        }
        return detections;
    }
}
