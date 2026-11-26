package com.sofiauni.sqlanalyzer.analyzer.correlation;

/**
 * Thread-local carrier for the correlation identifier of the current HTTP request
 * (section 4.2 {@code requestId}). The value is read on the request thread by the query
 * listener and copied into each {@code QueryEvent} before it crosses onto a worker thread.
 */
public final class RequestContext {

    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();

    private RequestContext() {
    }

    public static void setRequestId(String requestId) {
        REQUEST_ID.set(requestId);
    }

    public static String getRequestId() {
        return REQUEST_ID.get();
    }

    public static void clear() {
        REQUEST_ID.remove();
    }
}
