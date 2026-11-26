package com.sofiauni.sqlanalyzer.analyzer.pipeline;

import com.sofiauni.sqlanalyzer.analyzer.model.QueryEvent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bounded, lock-free-ish handoff between the request thread and the analysis workers
 * (section 4.4 step 3). On overflow it applies a drop-newest policy and increments a
 * counter instead of blocking the request thread — directly implementing the principle
 * that analyzer overhead must never become correlated with the request critical path.
 */
public class EventQueue {

    private final BlockingQueue<QueueItem> queue;
    private final AtomicLong droppedCount = new AtomicLong();

    public EventQueue(int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    public void offerEvent(QueryEvent event) {
        if (!queue.offer(new QueueItem.Query(event))) {
            droppedCount.incrementAndGet();
        }
    }

    public void offerRequestEnd(String requestId) {
        if (!queue.offer(new QueueItem.RequestEnd(requestId))) {
            droppedCount.incrementAndGet();
        }
    }

    public QueueItem poll(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    public int size() {
        return queue.size();
    }

    public long droppedCount() {
        return droppedCount.get();
    }
}
