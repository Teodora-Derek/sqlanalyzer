package com.sofiauni.sqlanalyzer.analyzer.detection;

import com.sofiauni.sqlanalyzer.analyzer.model.Detection;
import com.sofiauni.sqlanalyzer.analyzer.model.QueryEvent;

import java.util.List;

/**
 * A detector that inspects the full, ordered sequence of queries belonging to a single
 * completed HTTP request (section 4.4 step 5). Per-request correlation is what makes
 * patterns such as N+1 detectable (hypothesis H3).
 */
public interface RequestScopedDetector {

    List<Detection> inspect(String requestId, List<QueryEvent> events);
}
