package com.sofiauni.sqlanalyzer.analyzer.interception;

import com.sofiauni.sqlanalyzer.analyzer.collector.EventCollector;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;

import java.util.List;

/**
 * Interception point (section 4.4 step 1). Implemented as a {@code datasource-proxy}
 * listener so every JDBC execution — regardless of whether it originates from JPA/Hibernate,
 * JOOQ or plain JDBC — is observed at the {@code DataSource} layer, above the driver.
 */
public class QueryEventListener implements QueryExecutionListener {

    private final EventCollector collector;

    public QueryEventListener(EventCollector collector) {
        this.collector = collector;
    }

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        // No work before execution; timing is provided by datasource-proxy after the call.
    }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        long durationNs = execInfo.getElapsedTime() * 1_000_000L;
        long rowCount = extractRowCount(execInfo);
        String transactionId = String.valueOf(execInfo.getConnectionId());

        for (QueryInfo queryInfo : queryInfoList) {
            collector.collect(queryInfo.getQuery(), durationNs, rowCount, transactionId);
        }
    }

    private long extractRowCount(ExecutionInfo execInfo) {
        Object result = execInfo.getResult();
        if (result instanceof Integer affected) {
            return affected.longValue();
        }
        if (result instanceof Long affected) {
            return affected;
        }
        if (result instanceof int[] batch) {
            long sum = 0;
            for (int value : batch) {
                if (value > 0) {
                    sum += value;
                }
            }
            return sum;
        }
        return -1;
    }
}
