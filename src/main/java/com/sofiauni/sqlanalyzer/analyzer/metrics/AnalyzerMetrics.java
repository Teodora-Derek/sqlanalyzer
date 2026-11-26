package com.sofiauni.sqlanalyzer.analyzer.metrics;

import com.sofiauni.sqlanalyzer.analyzer.model.DetectionType;

/**
 * Metrics abstraction so the core has no hard dependency on Micrometer. A no-op implementation
 * is used unless a {@code MeterRegistry} is present (section 4.6).
 */
public interface AnalyzerMetrics {

    void recordDetection(DetectionType type);
}
