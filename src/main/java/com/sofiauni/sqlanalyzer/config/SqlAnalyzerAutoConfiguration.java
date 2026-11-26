package com.sofiauni.sqlanalyzer.config;

import com.sofiauni.sqlanalyzer.analyzer.aggregation.Aggregator;
import com.sofiauni.sqlanalyzer.analyzer.collector.EventCollector;
import com.sofiauni.sqlanalyzer.analyzer.collector.SqlNormalizer;
import com.sofiauni.sqlanalyzer.analyzer.correlation.Correlator;
import com.sofiauni.sqlanalyzer.analyzer.detection.AggregateDetector;
import com.sofiauni.sqlanalyzer.analyzer.detection.DetectionEngine;
import com.sofiauni.sqlanalyzer.analyzer.detection.RequestScopedDetector;
import com.sofiauni.sqlanalyzer.analyzer.detection.impl.BulkInsteadOfBatchDetector;
import com.sofiauni.sqlanalyzer.analyzer.detection.impl.LongRunningQueryDetector;
import com.sofiauni.sqlanalyzer.analyzer.detection.impl.MissingPaginationDetector;
import com.sofiauni.sqlanalyzer.analyzer.detection.impl.NPlusOneDetector;
import com.sofiauni.sqlanalyzer.analyzer.detection.impl.RepeatedQueryDetector;
import com.sofiauni.sqlanalyzer.analyzer.detection.impl.SelectStarDetector;
import com.sofiauni.sqlanalyzer.analyzer.detection.impl.TransactionSpanAnomalyDetector;
import com.sofiauni.sqlanalyzer.analyzer.interception.DataSourceProxyBeanPostProcessor;
import com.sofiauni.sqlanalyzer.analyzer.interception.QueryEventListener;
import com.sofiauni.sqlanalyzer.analyzer.metrics.AnalyzerMetrics;
import com.sofiauni.sqlanalyzer.analyzer.metrics.NoOpAnalyzerMetrics;
import com.sofiauni.sqlanalyzer.analyzer.pipeline.AnalysisDispatcher;
import com.sofiauni.sqlanalyzer.analyzer.pipeline.EventQueue;
import com.sofiauni.sqlanalyzer.analyzer.recommendation.RecommendationEngine;
import com.sofiauni.sqlanalyzer.analyzer.report.ReportSink;
import com.sofiauni.sqlanalyzer.analyzer.report.ReportStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Core auto-configuration that wires the whole analyzer pipeline (sections 4.3 and 4.4) and
 * transparently proxies the application {@code DataSource}. Activated by default; can be turned
 * off with {@code sqlanalyzer.enabled=false}.
 */
@AutoConfiguration(after = SqlAnalyzerMetricsConfiguration.class)
@ConditionalOnProperty(prefix = "sqlanalyzer", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(SqlAnalyzerProperties.class)
public class SqlAnalyzerAutoConfiguration {

    private static final long AGGREGATION_WINDOW_MS = 60_000;

    @Bean
    @ConditionalOnMissingBean
    public SqlNormalizer sqlNormalizer() {
        return new SqlNormalizer();
    }

    @Bean
    @ConditionalOnMissingBean
    public EventQueue sqlAnalyzerEventQueue(SqlAnalyzerProperties properties) {
        return new EventQueue(properties.getQueue().getCapacity());
    }

    @Bean
    @ConditionalOnMissingBean
    public EventCollector eventCollector(SqlNormalizer normalizer, EventQueue eventQueue, SqlAnalyzerProperties properties) {
        return new EventCollector(normalizer, eventQueue, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public QueryEventListener queryEventListener(EventCollector collector) {
        return new QueryEventListener(collector);
    }

    @Bean
    static DataSourceProxyBeanPostProcessor dataSourceProxyBeanPostProcessor(ObjectProvider<QueryEventListener> listener) {
        return new DataSourceProxyBeanPostProcessor(listener::getObject);
    }

    @Bean
    @ConditionalOnMissingBean
    public Aggregator aggregator() {
        return new Aggregator(AGGREGATION_WINDOW_MS);
    }

    @Bean
    public NPlusOneDetector nPlusOneDetector(SqlAnalyzerProperties properties) {
        return new NPlusOneDetector(properties.getDetectors().getNPlusOneThreshold());
    }

    @Bean
    public RepeatedQueryDetector repeatedQueryDetector(SqlAnalyzerProperties properties) {
        return new RepeatedQueryDetector(properties.getDetectors().getRepeatedQueryThreshold(),
                properties.getDetectors().getNPlusOneThreshold());
    }

    @Bean
    public MissingPaginationDetector missingPaginationDetector(SqlAnalyzerProperties properties) {
        return new MissingPaginationDetector(properties.getDetectors().getMissingPaginationRowCount());
    }

    @Bean
    public SelectStarDetector selectStarDetector() {
        return new SelectStarDetector();
    }

    @Bean
    public BulkInsteadOfBatchDetector bulkInsteadOfBatchDetector(SqlAnalyzerProperties properties) {
        return new BulkInsteadOfBatchDetector(properties.getDetectors().getBulkThreshold());
    }

    @Bean
    public TransactionSpanAnomalyDetector transactionSpanAnomalyDetector(SqlAnalyzerProperties properties) {
        return new TransactionSpanAnomalyDetector(properties.getDetectors().getTransactionSpanThreshold());
    }

    @Bean
    public LongRunningQueryDetector longRunningQueryDetector(SqlAnalyzerProperties properties) {
        return new LongRunningQueryDetector(properties.getDetectors().getLongRunningMillis());
    }

    @Bean
    @ConditionalOnMissingBean
    public ReportStore reportStore(SqlAnalyzerProperties properties) {
        return new ReportStore(properties.getReport().getCapacity());
    }

    @Bean
    @ConditionalOnMissingBean
    public ReportSink reportSink() {
        return recommendation -> {
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public AnalyzerMetrics analyzerMetrics() {
        return new NoOpAnalyzerMetrics();
    }

    @Bean
    @ConditionalOnMissingBean
    public RecommendationEngine recommendationEngine(ReportStore reportStore, ReportSink reportSink, AnalyzerMetrics metrics) {
        return new RecommendationEngine(reportStore, reportSink, metrics);
    }

    @Bean
    @ConditionalOnMissingBean
    public DetectionEngine detectionEngine(List<RequestScopedDetector> requestScopedDetectors,
                                           List<AggregateDetector> aggregateDetectors,
                                           RecommendationEngine recommendationEngine) {
        return new DetectionEngine(requestScopedDetectors, aggregateDetectors, recommendationEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    public Correlator correlator(DetectionEngine detectionEngine) {
        return new Correlator(detectionEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    public AnalysisDispatcher analysisDispatcher(EventQueue eventQueue,
                                                 Correlator correlator,
                                                 Aggregator aggregator,
                                                 DetectionEngine detectionEngine,
                                                 SqlAnalyzerProperties properties) {
        return new AnalysisDispatcher(eventQueue, correlator, aggregator, detectionEngine,
                properties.getQueue().getWorkers());
    }
}
