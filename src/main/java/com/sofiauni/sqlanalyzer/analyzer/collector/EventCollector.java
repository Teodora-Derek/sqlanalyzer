package com.sofiauni.sqlanalyzer.analyzer.collector;

import com.sofiauni.sqlanalyzer.analyzer.correlation.RequestContext;
import com.sofiauni.sqlanalyzer.analyzer.model.QueryEvent;
import com.sofiauni.sqlanalyzer.analyzer.model.QueryType;
import com.sofiauni.sqlanalyzer.analyzer.pipeline.EventQueue;
import com.sofiauni.sqlanalyzer.config.SqlAnalyzerProperties;

/**
 * Lightweight pre-processing performed on the request thread (section 4.4 step 2):
 * normalize, hash, read the correlation id, assemble a {@link QueryEvent} and hand it to
 * the async queue. Deliberately cheap — all heavy analysis happens downstream.
 */
public class EventCollector {

    private final SqlNormalizer normalizer;
    private final EventQueue eventQueue;
    private final SqlAnalyzerProperties properties;

    public EventCollector(SqlNormalizer normalizer, EventQueue eventQueue, SqlAnalyzerProperties properties) {
        this.normalizer = normalizer;
        this.eventQueue = eventQueue;
        this.properties = properties;
    }

    public void collect(String rawSql, long durationNs, long rowCount, String transactionId) {
        String normalized = normalizer.normalize(rawSql);
        String sqlHash = normalizer.hash(normalized);
        QueryType type = normalizer.detectType(rawSql);

        QueryEvent event = new QueryEvent(
                properties.isCaptureRawSql() ? rawSql : null,
                normalized,
                sqlHash,
                type,
                durationNs,
                rowCount,
                System.nanoTime(),
                transactionId,
                RequestContext.getRequestId(),
                properties.isCaptureStackHint() ? captureStackHint() : null
        );
        eventQueue.offerEvent(event);
    }

    private String captureStackHint() {
        return StackWalker.getInstance()
                .walk(frames -> frames
                        .map(StackWalker.StackFrame::getClassName)
                        .filter(name -> !name.startsWith("com.sofiauni.sqlanalyzer")
                                && !name.startsWith("net.ttddyy")
                                && !name.startsWith("org.springframework")
                                && !name.startsWith("org.hibernate")
                                && !name.startsWith("jdk.")
                                && !name.startsWith("java."))
                        .findFirst()
                        .orElse(null));
    }
}
