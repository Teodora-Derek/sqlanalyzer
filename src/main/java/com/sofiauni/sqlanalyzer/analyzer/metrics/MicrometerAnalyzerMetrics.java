package com.sofiauni.sqlanalyzer.analyzer.metrics;

import com.sofiauni.sqlanalyzer.analyzer.model.DetectionType;
import com.sofiauni.sqlanalyzer.analyzer.pipeline.EventQueue;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.EnumMap;
import java.util.Map;

/**
 * Micrometer-backed metrics: per-type detection counters plus gauges for queue depth and
 * dropped events (section 4.6). Only created when a {@code MeterRegistry} bean exists.
 */
public class MicrometerAnalyzerMetrics implements AnalyzerMetrics {

    private final Map<DetectionType, Counter> detectionCounters = new EnumMap<>(DetectionType.class);

    public MicrometerAnalyzerMetrics(MeterRegistry registry, EventQueue eventQueue) {
        for (DetectionType type : DetectionType.values()) {
            detectionCounters.put(type, Counter.builder("sqlanalyzer.detections")
                    .tag("type", type.name().toLowerCase())
                    .description("Number of detections by type")
                    .register(registry));
        }
        registry.gauge("sqlanalyzer.queue.size", eventQueue, EventQueue::size);
        registry.gauge("sqlanalyzer.queue.dropped", eventQueue, EventQueue::droppedCount);
    }

    @Override
    public void recordDetection(DetectionType type) {
        Counter counter = detectionCounters.get(type);
        if (counter != null) {
            counter.increment();
        }
    }
}
