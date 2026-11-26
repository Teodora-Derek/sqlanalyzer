package com.sofiauni.sqlanalyzer.analyzer.report;

import com.sofiauni.sqlanalyzer.analyzer.model.Recommendation;

/**
 * Pluggable export target for recommendations (section 4.6). The default is a no-op; a consumer
 * may provide a bean to persist to a database, emit JSON for CI/CD, etc.
 */
@FunctionalInterface
public interface ReportSink {

    void export(Recommendation recommendation);
}
