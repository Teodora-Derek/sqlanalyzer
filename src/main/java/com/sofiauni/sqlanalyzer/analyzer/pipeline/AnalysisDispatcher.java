package com.sofiauni.sqlanalyzer.analyzer.pipeline;

import com.sofiauni.sqlanalyzer.analyzer.aggregation.Aggregator;
import com.sofiauni.sqlanalyzer.analyzer.correlation.Correlator;
import com.sofiauni.sqlanalyzer.analyzer.detection.DetectionEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The cold path (section 4.4 step 4). Drains {@link EventQueue} on a pool of Java 21 virtual
 * threads, feeding each event into the {@link Correlator} and {@link Aggregator}, and runs
 * aggregate detectors on a periodic schedule. Nothing here executes on the request thread,
 * which is the core of hypothesis H1 (asynchronous processing).
 */
public class AnalysisDispatcher implements SmartLifecycle {

    private static final Logger LOG = LoggerFactory.getLogger(AnalysisDispatcher.class);
    private static final long POLL_TIMEOUT_MS = 200;
    private static final long AGGREGATE_INTERVAL_S = 30;

    private final EventQueue eventQueue;
    private final Correlator correlator;
    private final Aggregator aggregator;
    private final DetectionEngine detectionEngine;
    private final int workerCount;

    private volatile boolean running = false;
    private ExecutorService workers;
    private ScheduledExecutorService scheduler;

    public AnalysisDispatcher(EventQueue eventQueue,
                              Correlator correlator,
                              Aggregator aggregator,
                              DetectionEngine detectionEngine,
                              int workerCount) {
        this.eventQueue = eventQueue;
        this.correlator = correlator;
        this.aggregator = aggregator;
        this.detectionEngine = detectionEngine;
        this.workerCount = workerCount;
    }

    @Override
    public void start() {
        running = true;
        workers = Executors.newVirtualThreadPerTaskExecutor();
        for (int i = 0; i < workerCount; i++) {
            workers.submit(this::drainLoop);
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "sqlanalyzer-aggregate");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(this::runAggregate,
                AGGREGATE_INTERVAL_S, AGGREGATE_INTERVAL_S, TimeUnit.SECONDS);
        LOG.info("SQL analyzer dispatcher started [workers={}]", workerCount);
    }

    private void drainLoop() {
        while (running) {
            try {
                QueueItem item = eventQueue.poll(POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                if (item != null) {
                    dispatch(item);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (RuntimeException e) {
                LOG.warn("SQL analyzer worker failed to process an item [error={}]", e.toString());
            }
        }
    }

    private void dispatch(QueueItem item) {
        switch (item) {
            case QueueItem.Query query -> {
                correlator.onEvent(query.event());
                aggregator.record(query.event());
            }
            case QueueItem.RequestEnd requestEnd -> correlator.onRequestEnd(requestEnd.requestId());
        }
    }

    private void runAggregate() {
        try {
            detectionEngine.analyzeAggregate(aggregator);
        } catch (RuntimeException e) {
            LOG.warn("SQL analyzer aggregate pass failed [error={}]", e.toString());
        }
    }

    @Override
    public void stop() {
        running = false;
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        if (workers != null) {
            workers.shutdownNow();
        }
        LOG.info("SQL analyzer dispatcher stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
