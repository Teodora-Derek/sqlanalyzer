package com.sofiauni.sqlanalyzer.analyzer.interception;

import net.ttddyy.dsproxy.support.ProxyDataSource;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import javax.sql.DataSource;
import java.util.function.Supplier;

/**
 * Transparently wraps every {@link DataSource} bean in the application context with a
 * {@code datasource-proxy} that routes executions through {@link QueryEventListener}.
 *
 * <p>This is the manual wrapping approach (no auto-config starter): it requires zero changes
 * to consumer business code, satisfying the integration criterion C5. The listener is obtained
 * lazily so that registering this {@code BeanPostProcessor} does not force the analyzer beans to
 * initialize prematurely.
 */
public class DataSourceProxyBeanPostProcessor implements BeanPostProcessor {

    private final Supplier<QueryEventListener> listenerSupplier;
    private volatile QueryEventListener listener;

    public DataSourceProxyBeanPostProcessor(Supplier<QueryEventListener> listenerSupplier) {
        this.listenerSupplier = listenerSupplier;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource dataSource && !(bean instanceof ProxyDataSource)) {
            return ProxyDataSourceBuilder.create(dataSource)
                    .name("sqlanalyzer")
                    .listener(resolveListener())
                    .build();
        }
        return bean;
    }

    private QueryEventListener resolveListener() {
        QueryEventListener resolved = listener;
        if (resolved == null) {
            resolved = listenerSupplier.get();
            listener = resolved;
        }
        return resolved;
    }
}
