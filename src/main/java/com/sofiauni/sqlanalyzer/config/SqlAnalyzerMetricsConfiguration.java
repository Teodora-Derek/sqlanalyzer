package com.sofiauni.sqlanalyzer.config;

import com.sofiauni.sqlanalyzer.analyzer.metrics.AnalyzerMetrics;
import com.sofiauni.sqlanalyzer.analyzer.metrics.MicrometerAnalyzerMetrics;
import com.sofiauni.sqlanalyzer.analyzer.metrics.NoOpAnalyzerMetrics;
import com.sofiauni.sqlanalyzer.analyzer.pipeline.EventQueue;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * Optional metrics wiring (section 4.6). Active only when Micrometer is on the classpath; binds a
 * real {@link MeterRegistry} when one exists, otherwise falls back to a no-op so the rest of the
 * pipeline is unaffected.
 */
@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnProperty(prefix = "sqlanalyzer", name = "enabled", matchIfMissing = true)
public class SqlAnalyzerMetricsConfiguration {

    @Bean
    @ConditionalOnMissingBean(AnalyzerMetrics.class)
    public AnalyzerMetrics analyzerMetrics(ObjectProvider<MeterRegistry> meterRegistry, EventQueue eventQueue) {
        MeterRegistry registry = meterRegistry.getIfAvailable();
        return registry != null
                ? new MicrometerAnalyzerMetrics(registry, eventQueue)
                : new NoOpAnalyzerMetrics();
    }
}
