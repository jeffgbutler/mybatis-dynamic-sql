package org.mybatis.dynamic.sql.render;

import org.mybatis.dynamic.sql.BindableColumn;

import java.util.concurrent.atomic.AtomicInteger;

public class AbstractIndexedR2DBCRenderingStrategy extends AbstractR2DBCRenderingStrategy {
    private final String prefix;

    protected AbstractIndexedR2DBCRenderingStrategy(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String formatParameterMapKey(AtomicInteger sequence) {
        return prefix + sequence.getAndIncrement();
    }

    @Override
    public String getFormattedJdbcPlaceholder(BindableColumn<?> column, String prefix, String parameterName) {
        return getFormattedJdbcPlaceholder(prefix, parameterName);
    }

    @Override
    public String getFormattedJdbcPlaceholder(String prefix, String parameterName) {
        return parameterName;
    }
}
