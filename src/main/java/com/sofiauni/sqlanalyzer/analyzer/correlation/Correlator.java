package com.sofiauni.sqlanalyzer.analyzer.correlation;

import com.sofiauni.sqlanalyzer.analyzer.detection.DetectionEngine;
import com.sofiauni.sqlanalyzer.analyzer.model.QueryEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Groups queries by their originating {@code requestId} (section 4.4 step 4). When a request
 * window closes it hands the full ordered sequence to the {@link DetectionEngine} and evicts
 * the window. Events without a correlation id are ignored here (they still feed the aggregator).
 */
public class Correlator {

    private final ConcurrentHashMap<String, Queue<QueryEvent>> activeRequests = new ConcurrentHashMap<>();
    private final DetectionEngine detectionEngine;

    public Correlator(DetectionEngine detectionEngine) {
        this.detectionEngine = detectionEngine;
    }

    public void onEvent(QueryEvent event) {
        String requestId = event.requestId();
        if (requestId == null) {
            return;
        }
        activeRequests.computeIfAbsent(requestId, key -> new ConcurrentLinkedQueue<>()).add(event);
    }

    public void onRequestEnd(String requestId) {
        Queue<QueryEvent> events = activeRequests.remove(requestId);
        if (events == null || events.isEmpty()) {
            return;
        }
        detectionEngine.analyzeRequest(requestId, new ArrayList<>(events));
    }
}
