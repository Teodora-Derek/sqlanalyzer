package com.sofiauni.sqlanalyzer.analyzer.aggregation;

import com.sofiauni.sqlanalyzer.analyzer.model.QueryEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Maintains per-{@code sqlHash} statistics over a sliding time window (section 4.4 step 4).
 * Feeds the global, aggregate-type detectors (e.g. long-running query percentiles).
 */
public class Aggregator {

    /** Statistics for one query shape over the current window. */
    public record HashStats(String sqlHash, String sampleSql, int count, long p95Nanos) {
    }

    private record Sample(long epochMillis, long durationNs) {
    }

    private final long windowMillis;
    private final ConcurrentHashMap<String, ConcurrentLinkedDeque<Sample>> samples = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sampleSqlByHash = new ConcurrentHashMap<>();

    public Aggregator(long windowMillis) {
        this.windowMillis = windowMillis;
    }

    public void record(QueryEvent event) {
        if (event.sqlHash() == null) {
            return;
        }
        samples.computeIfAbsent(event.sqlHash(), key -> new ConcurrentLinkedDeque<>())
                .add(new Sample(System.currentTimeMillis(), event.durationNs()));
        sampleSqlByHash.putIfAbsent(event.sqlHash(), event.normalizedSql());
    }

    public List<HashStats> windowStats() {
        long cutoff = System.currentTimeMillis() - windowMillis;
        List<HashStats> result = new ArrayList<>();
        samples.forEach((hash, deque) -> {
            prune(deque, cutoff);
            List<Long> durations = new ArrayList<>();
            for (Sample sample : deque) {
                durations.add(sample.durationNs());
            }
            if (!durations.isEmpty()) {
                result.add(new HashStats(hash, sampleSqlByHash.get(hash), durations.size(), percentile(durations, 95)));
            }
        });
        return result;
    }

    private void prune(ConcurrentLinkedDeque<Sample> deque, long cutoff) {
        Sample head;
        while ((head = deque.peekFirst()) != null && head.epochMillis() < cutoff) {
            deque.pollFirst();
        }
    }

    private long percentile(List<Long> durations, int percentile) {
        List<Long> sorted = new ArrayList<>(durations);
        sorted.sort(Long::compareTo);
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        index = Math.max(0, Math.min(index, sorted.size() - 1));
        return sorted.get(index);
    }
}
