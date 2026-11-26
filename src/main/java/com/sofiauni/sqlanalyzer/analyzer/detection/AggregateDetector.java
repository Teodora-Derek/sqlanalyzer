package com.sofiauni.sqlanalyzer.analyzer.detection;

import com.sofiauni.sqlanalyzer.analyzer.aggregation.Aggregator;
import com.sofiauni.sqlanalyzer.analyzer.model.Detection;

import java.util.List;

/**
 * A detector that runs periodically over time-windowed aggregate statistics rather than a
 * single request (section 4.4 step 5), e.g. percentile-based long-running query detection.
 */
public interface AggregateDetector {

    List<Detection> inspect(Aggregator aggregator);
}
