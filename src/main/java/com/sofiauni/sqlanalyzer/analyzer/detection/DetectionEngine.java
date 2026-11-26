package com.sofiauni.sqlanalyzer.analyzer.detection;

import com.sofiauni.sqlanalyzer.analyzer.aggregation.Aggregator;
import com.sofiauni.sqlanalyzer.analyzer.model.Detection;
import com.sofiauni.sqlanalyzer.analyzer.model.QueryEvent;
import com.sofiauni.sqlanalyzer.analyzer.recommendation.RecommendationEngine;

import java.util.List;

/**
 * Orchestrates the detector catalog (section 4.5). Runs request-scoped detectors when a
 * request window closes and aggregate detectors periodically, forwarding every positive
 * detection to the recommendation engine.
 */
public class DetectionEngine {

    private final List<RequestScopedDetector> requestScopedDetectors;
    private final List<AggregateDetector> aggregateDetectors;
    private final RecommendationEngine recommendationEngine;

    public DetectionEngine(List<RequestScopedDetector> requestScopedDetectors,
                           List<AggregateDetector> aggregateDetectors,
                           RecommendationEngine recommendationEngine) {
        this.requestScopedDetectors = requestScopedDetectors;
        this.aggregateDetectors = aggregateDetectors;
        this.recommendationEngine = recommendationEngine;
    }

    public void analyzeRequest(String requestId, List<QueryEvent> events) {
        for (RequestScopedDetector detector : requestScopedDetectors) {
            for (Detection detection : detector.inspect(requestId, events)) {
                recommendationEngine.handle(detection);
            }
        }
    }

    public void analyzeAggregate(Aggregator aggregator) {
        for (AggregateDetector detector : aggregateDetectors) {
            for (Detection detection : detector.inspect(aggregator)) {
                recommendationEngine.handle(detection);
            }
        }
    }
}
