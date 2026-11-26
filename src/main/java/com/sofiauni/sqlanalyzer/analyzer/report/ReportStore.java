package com.sofiauni.sqlanalyzer.analyzer.report;

import com.sofiauni.sqlanalyzer.analyzer.model.DetectionType;
import com.sofiauni.sqlanalyzer.analyzer.model.Recommendation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Bounded in-memory store of the most recent recommendations (section 4.6). Acts as the default
 * report sink so the library has no hard persistence dependency.
 */
public class ReportStore {

    private final int capacity;
    private final Deque<Recommendation> recommendations = new ArrayDeque<>();
    private final Map<DetectionType, Long> countsByType = new EnumMap<>(DetectionType.class);

    public ReportStore(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void add(Recommendation recommendation) {
        recommendations.addFirst(recommendation);
        countsByType.merge(recommendation.type(), 1L, Long::sum);
        while (recommendations.size() > capacity) {
            recommendations.removeLast();
        }
    }

    public synchronized List<Recommendation> findAll() {
        return new ArrayList<>(recommendations);
    }

    public synchronized Map<DetectionType, Long> countsByType() {
        return new EnumMap<>(countsByType);
    }

    public synchronized void clear() {
        recommendations.clear();
        countsByType.clear();
    }
}
