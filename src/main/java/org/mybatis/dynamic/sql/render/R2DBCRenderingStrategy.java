package org.mybatis.dynamic.sql.render;

import org.mybatis.dynamic.sql.BindableColumn;

import java.util.concurrent.atomic.AtomicInteger;

public class R2DBCRenderingStrategy extends RenderingStrategy {

    @Override
    public String formatParameterMapKey(AtomicInteger sequence) {
        return "$" + sequence.getAndIncrement(); //$NON-NLS-1$
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
