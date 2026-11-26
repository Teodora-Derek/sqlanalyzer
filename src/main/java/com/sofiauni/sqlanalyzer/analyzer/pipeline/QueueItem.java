package com.sofiauni.sqlanalyzer.analyzer.pipeline;

import com.sofiauni.sqlanalyzer.analyzer.model.QueryEvent;

/**
 * Unit of work carried across the async boundary between the request thread and the
 * analysis workers (section 4.4). Either a captured query or an end-of-request marker
 * signalling the correlator to close and analyse a request window.
 */
public sealed interface QueueItem permits QueueItem.Query, QueueItem.RequestEnd {

    record Query(QueryEvent event) implements QueueItem {
    }

    record RequestEnd(String requestId) implements QueueItem {
    }
}
