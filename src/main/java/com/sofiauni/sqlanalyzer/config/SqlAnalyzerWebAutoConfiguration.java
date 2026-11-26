package com.sofiauni.sqlanalyzer.config;

import com.sofiauni.sqlanalyzer.analyzer.correlation.RequestCorrelationFilter;
import com.sofiauni.sqlanalyzer.analyzer.pipeline.EventQueue;
import com.sofiauni.sqlanalyzer.analyzer.report.ReportController;
import com.sofiauni.sqlanalyzer.analyzer.report.ReportStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Web-only wiring (section 4.4). Registers the request-correlation filter (run first so the
 * correlation id is set before any query executes) and the optional read-only report endpoint.
 */
@AutoConfiguration(after = SqlAnalyzerAutoConfiguration.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(OncePerRequestFilter.class)
@ConditionalOnProperty(prefix = "sqlanalyzer", name = "enabled", matchIfMissing = true)
public class SqlAnalyzerWebAutoConfiguration {

    @Bean
    public FilterRegistrationBean<RequestCorrelationFilter> requestCorrelationFilter(EventQueue eventQueue) {
        FilterRegistrationBean<RequestCorrelationFilter> registration =
                new FilterRegistrationBean<>(new RequestCorrelationFilter(eventQueue));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    @ConditionalOnProperty(prefix = "sqlanalyzer.report", name = "endpoint-enabled", matchIfMissing = true)
    public ReportController sqlAnalyzerReportController(ReportStore reportStore) {
        return new ReportController(reportStore);
    }
}
