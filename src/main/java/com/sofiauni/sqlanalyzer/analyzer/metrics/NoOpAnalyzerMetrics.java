package com.sofiauni.sqlanalyzer.analyzer.metrics;

import com.sofiauni.sqlanalyzer.analyzer.model.DetectionType;

public class NoOpAnalyzerMetrics implements AnalyzerMetrics {

    @Override
    public void recordDetection(DetectionType type) {
        // no-op
    }
}
