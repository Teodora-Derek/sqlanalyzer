package com.sofiauni.sqlanalyzer.analyzer.detection.impl;

import com.sofiauni.sqlanalyzer.analyzer.detection.RequestScopedDetector;
import com.sofiauni.sqlanalyzer.analyzer.model.Detection;
import com.sofiauni.sqlanalyzer.analyzer.model.DetectionType;
import com.sofiauni.sqlanalyzer.analyzer.model.QueryEvent;
import com.sofiauni.sqlanalyzer.analyzer.model.QueryType;
import com.sofiauni.sqlanalyzer.analyzer.model.Severity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Missing pagination detector (section 4.5). Flags unbounded SELECTs: either a returned row
 * count above the threshold (when the driver reports it), or a full scan with no LIMIT and no
 * WHERE clause. Row count is best-effort per the limitations in section 4.7.
 */
public class MissingPaginationDetector implements RequestScopedDetector {

    private final long rowCountThreshold;

    public MissingPaginationDetector(long rowCountThreshold) {
        this.rowCountThreshold = rowCountThreshold;
    }

    @Override
    public List<Detection> inspect(String requestId, List<QueryEvent> events) {
        List<Detection> detections = new ArrayList<>();
        Set<String> reported = new HashSet<>();

        for (QueryEvent event : events) {
            if (event.queryType() != QueryType.SELECT
                    || SqlPatterns.hasLimit(event.normalizedSql())
                    || SqlPatterns.isAggregateOnly(event.normalizedSql())
                    || !reported.add(event.sqlHash())) {
                continue;
            }

            boolean overRowThreshold = event.rowCount() >= rowCountThreshold;
            boolean unboundedScan = event.rowCount() < 0 && !SqlPatterns.hasWhere(event.normalizedSql());
            if (overRowThreshold || unboundedScan) {
                detections.add(new Detection(
                        DetectionType.MISSING_PAGINATION,
                        overRowThreshold ? Severity.HIGH : Severity.MEDIUM,
                        requestId,
                        1,
                        List.of(event.normalizedSql()),
                        overRowThreshold
                                ? "Unbounded SELECT returned %d rows".formatted(event.rowCount())
                                : "Unbounded SELECT without WHERE or LIMIT clause",
                        Instant.now()
                ));
            }
        }
        return detections;
    }
}
