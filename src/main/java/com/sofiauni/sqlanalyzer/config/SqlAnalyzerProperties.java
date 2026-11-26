package com.sofiauni.sqlanalyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Externalised configuration for the analyzer. All thresholds are per-application
 * configurable, as required by the methodology (section 4.5): "what is an anomaly in
 * one service may be normal in another".
 */
@ConfigurationProperties(prefix = "sqlanalyzer")
public class SqlAnalyzerProperties {

    /** Master switch for the whole library. */
    private boolean enabled = true;

    /** Capture the raw (un-normalized) SQL on each event. High cardinality; can be disabled in prod. */
    private boolean captureRawSql = true;

    /** Capture a short stack hint locating the originating application class. */
    private boolean captureStackHint = false;

    @NestedConfigurationProperty
    private final Queue queue = new Queue();

    @NestedConfigurationProperty
    private final Detectors detectors = new Detectors();

    @NestedConfigurationProperty
    private final Report report = new Report();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isCaptureRawSql() {
        return captureRawSql;
    }

    public void setCaptureRawSql(boolean captureRawSql) {
        this.captureRawSql = captureRawSql;
    }

    public boolean isCaptureStackHint() {
        return captureStackHint;
    }

    public void setCaptureStackHint(boolean captureStackHint) {
        this.captureStackHint = captureStackHint;
    }

    public Queue getQueue() {
        return queue;
    }

    public Detectors getDetectors() {
        return detectors;
    }

    public Report getReport() {
        return report;
    }

    /** Bounded async queue + worker pool (section 4.3, 4.4). */
    public static class Queue {
        /** Maximum number of buffered events before drop-newest kicks in. */
        private int capacity = 10_000;
        /** Number of virtual-thread analysis workers draining the queue. */
        private int workers = 2;

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public int getWorkers() {
            return workers;
        }

        public void setWorkers(int workers) {
            this.workers = workers;
        }
    }

    /** Thresholds for the detector catalog (section 4.5). */
    public static class Detectors {
        /** N+1: identical primary-key lookups within one request. */
        private int nPlusOneThreshold = 3;
        /** Repeated identical query within one request. */
        private int repeatedQueryThreshold = 2;
        /** Missing pagination: returned row count for an un-limited SELECT. */
        private long missingPaginationRowCount = 1_000;
        /** Long-running query threshold in milliseconds. */
        private long longRunningMillis = 500;
        /** Bulk-instead-of-batch: identical INSERT/UPDATE template within one request. */
        private int bulkThreshold = 10;
        /** Transaction span anomaly: number of statements within one transaction. */
        private int transactionSpanThreshold = 50;

        public int getNPlusOneThreshold() {
            return nPlusOneThreshold;
        }

        public void setNPlusOneThreshold(int nPlusOneThreshold) {
            this.nPlusOneThreshold = nPlusOneThreshold;
        }

        public int getRepeatedQueryThreshold() {
            return repeatedQueryThreshold;
        }

        public void setRepeatedQueryThreshold(int repeatedQueryThreshold) {
            this.repeatedQueryThreshold = repeatedQueryThreshold;
        }

        public long getMissingPaginationRowCount() {
            return missingPaginationRowCount;
        }

        public void setMissingPaginationRowCount(long missingPaginationRowCount) {
            this.missingPaginationRowCount = missingPaginationRowCount;
        }

        public long getLongRunningMillis() {
            return longRunningMillis;
        }

        public void setLongRunningMillis(long longRunningMillis) {
            this.longRunningMillis = longRunningMillis;
        }

        public int getBulkThreshold() {
            return bulkThreshold;
        }

        public void setBulkThreshold(int bulkThreshold) {
            this.bulkThreshold = bulkThreshold;
        }

        public int getTransactionSpanThreshold() {
            return transactionSpanThreshold;
        }

        public void setTransactionSpanThreshold(int transactionSpanThreshold) {
            this.transactionSpanThreshold = transactionSpanThreshold;
        }
    }

    /** In-memory report store + optional HTTP endpoint (section 4.6). */
    public static class Report {
        /** Maximum number of recommendations retained in the in-memory ring buffer. */
        private int capacity = 500;
        /** Expose the read-only report controller (only effective in web applications). */
        private boolean endpointEnabled = true;

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public boolean isEndpointEnabled() {
            return endpointEnabled;
        }

        public void setEndpointEnabled(boolean endpointEnabled) {
            this.endpointEnabled = endpointEnabled;
        }
    }
}
