package com.sofiauni.sqlanalyzer.analyzer.correlation;

import com.sofiauni.sqlanalyzer.analyzer.pipeline.EventQueue;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Assigns a correlation id to each HTTP request and, on completion, signals the analyzer to
 * close and analyse that request's query window (section 4.4 step 5; hypothesis H3).
 */
public class RequestCorrelationFilter extends OncePerRequestFilter {

    private static final String MDC_KEY = "sqlanalyzerRequestId";

    private final EventQueue eventQueue;

    public RequestCorrelationFilter(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        RequestContext.setRequestId(requestId);
        MDC.put(MDC_KEY, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            eventQueue.offerRequestEnd(requestId);
            RequestContext.clear();
            MDC.remove(MDC_KEY);
        }
    }
}
